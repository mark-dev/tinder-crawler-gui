package ru.gotinder.crawler.service;

import com.djm.tinder.Tinder;
import com.djm.tinder.like.Like;
import com.djm.tinder.like.SuperLike;
import com.djm.tinder.like.SuperLikeResponse;
import com.djm.tinder.pass.Pass;
import com.djm.tinder.user.User;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.gotinder.crawler.common.SyncVerdictResponse;
import ru.gotinder.crawler.enrich.HeightExtractorService;
import ru.gotinder.crawler.enrich.ScoringModelService;
import ru.gotinder.crawler.persistence.CrawlerDAO;
import ru.gotinder.crawler.persistence.dto.CrawlerDataDTO;
import ru.gotinder.crawler.persistence.dto.EnrichDataDTO;
import ru.gotinder.crawler.persistence.dto.VerdictEnum;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TinderCrawlerService {

    private static final Random RANDOM = new Random();

    @Autowired
    FacebookGateway fb;

    @Autowired
    CrawlerDAO dao;

    @Autowired
    ScoringModelService scoringService;

    @Autowired
    HeightExtractorService heightExtractorService;

    private Integer apiQueries = 0;

    @Value("${tinder.crawler.rating-print-treshold}")
    private Integer ratingPrintTreshold;

    @Value("${tinder.crawler.loops}")
    private Integer crawlerLoops;

    @SneakyThrows
    public Tinder getAPI() {
        return Tinder.fromAccessToken(fb.getToken());
    }

    @SneakyThrows
    public List<SyncVerdictResponse> syncVerdictsBatch(List<CrawlerDataDTO> dtos) {
        List<SyncVerdictResponse> responses = Collections.emptyList();

        if (!dtos.isEmpty()) {
            responses = new ArrayList<>(dtos.size());
            Tinder api = getAPI();
            boolean hasFailedSuperLikes = false;
            for (CrawlerDataDTO d : dtos) {
                SyncVerdictResponse vsr;
                if (d.getVerdict() == VerdictEnum.SUPERLIKE && hasFailedSuperLikes) {
                    log.info("Skipped superlike for {} due already has failed superlike in this batch", d.getId());
                    SuperLike superLike = new SuperLike();
                    superLike.setMatch(false);
                    superLike.setLimitExceeded(true);
                    superLike.setStatus(200);
                    vsr = new SyncVerdictResponse(false, superLike);
                } else {
                    vsr = syncVerdict(d, api);
                    if (!vsr.isSuccess() && d.getVerdict() == VerdictEnum.SUPERLIKE) {
                        hasFailedSuperLikes = true;
                    }
                    log.info("Sync verdict user: {}, verdict:{}, response: {}", d.getId(), d.getVerdict(), vsr);
                }
                responses.add(vsr);
                TimeUnit.MILLISECONDS.sleep(1000 + RANDOM.nextInt(2000));
            }
        }

        return responses;
    }

    /**
     * @param verdictBoundInclusive -
     *                              "правая" граница вердиктов, для реализации ограничения по типам вердиктов
     *                              Т.е. например, если указываем LIKE, то будут синхронизироваться только PASS и LIKE
     *                              Если указываем SUPERLIKE - будет синхронизироваться все (PASS,LIKE,SUPERLIKE)
     * @param limit                 Ограничение по количеству (чтоб не забанили)
     * @return
     */
    public List<SyncVerdictResponse> syncVerdictedItems(VerdictEnum verdictBoundInclusive, int limit) {
        List<CrawlerDataDTO> dtos = dao.loadVerdictedButNotSynced(verdictBoundInclusive, 0, limit);
        return syncVerdictsBatch(dtos);
    }


    public Integer crawNewData() {
        return crawNewData(ratingPrintTreshold);
    }

    public Integer crawNewData(Integer printRatingTreshold) {
        collectAndSaveRecs(crawlerLoops);

        return enrichData(printRatingTreshold);
    }

    public SyncVerdictResponse syncVerdict(String id) {
        CrawlerDataDTO obj = dao.byId(id).orElseThrow(() -> new RuntimeException("Object not found, id: " + id));
        Tinder api = getAPI();

        return syncVerdict(obj, api);

    }

    public SyncVerdictResponse syncVerdict(CrawlerDataDTO obj, Tinder api) {
        boolean success = false;
        Object ret = null;
        try {
            switch (obj.getVerdict()) {
                case LIKE:
                    Like like = api.like(obj.getId(), obj.getContentHash(), obj.getSNumber());
                    if (like.getStatus() == 200) {
                        success = true;
                    }
                    ret = like;
                    break;
                case PASS:
                    Pass pass = api.pass(obj.getId(), obj.getContentHash(), obj.getSNumber());
                    if (pass.getStatus() == 200) {
                        success = true;
                    }
                    ret = pass;
                    break;
                case SUPERLIKE:
                    SuperLikeResponse s = api.superLike(obj.getId());
                    ret = s.getSuperLike();
                    success = s.getSuperLike().isSuccessfully();
                    break;
                case UNDEFINED:
                    throw new RuntimeException("Verdict undefined, id: " + obj.getId());
            }
        } catch (Exception ex) {
            log.error(obj + " : exception while verdict sync.", ex);
        }


        if (success) {
            dao.updateVerdictTimestamp(obj.getId());
        }

        SyncVerdictResponse response = new SyncVerdictResponse(success, ret);
        return response;
    }

    public Integer enrichData() {
        return enrichData(null);
    }

    public Integer enrichData(Integer printRatingTreshold) {

        Integer enrichRequiredCountBefore = dao.countEnrichRequired();
        int limitPerQuery = 500; //TODO: В конфиг? Если на RaspberryPi запускать может тяжко выйти
        int step = 0;
        int processed = 0;

        List<CrawlerDataDTO> users = null;
        while (!(users = dao.loadEnrichRequired(limitPerQuery)).isEmpty()) {
            Map<String, EnrichDataDTO> ratingMap = new HashMap<>(users.size());
            for (CrawlerDataDTO u : users) {

                Integer rating = scoringService.evaluate(u);
                if (printRatingTreshold != null && rating >= printRatingTreshold) {
                    log.info("{} has good rating {}", u.getId(), rating);
                }

                Integer height = heightExtractorService.extractHeight(u);


                EnrichDataDTO dto = new EnrichDataDTO(rating, height);
                ratingMap.put(u.getId(), dto);

            }
            dao.enrichData(ratingMap);
            processed += users.size();
            log.info("Enrich step finished {}/{}", processed, enrichRequiredCountBefore);
        }

        Integer enrichRequiredAfter = dao.countEnrichRequired();
        return enrichRequiredCountBefore - enrichRequiredAfter;
    }

    @SneakyThrows
    private void collectAndSaveRecs(int loops) {
        Tinder api = getAPI();
        for (int i = 0; i < loops; i++) {
            log.info("Crawler recs collect loop: {}/{}", i + 1, loops);
            ArrayList<User> recs;
            try {
                recs = api.getRecommendations();
            } catch (Exception ex) {
                log.error("Exception while recomendation fetch: {}", ex.getClass());
                break;
            }
            dao.saveBatch(recs);
            Thread.sleep(1000);
        }
    }
}

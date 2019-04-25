package ru.gotinder.crawler.service;

import com.djm.tinder.Tinder;
import com.djm.tinder.like.Like;
import com.djm.tinder.like.SuperLikeResponse;
import com.djm.tinder.pass.Pass;
import com.djm.tinder.user.User;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gotinder.crawler.common.SyncVerdictResponse;
import ru.gotinder.crawler.persistence.CrawlerDAO;
import ru.gotinder.crawler.persistence.dto.CrawlerDataDTO;
import ru.gotinder.crawler.scoring.RatingEvaluator;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TinderCrawlerService {
    public static final int CRAWLER_LOOPS = 4;

    @Autowired
    FacebookGateway fb;

    @Autowired
    CrawlerDAO dao;

    @Autowired
    RatingEvaluator evaluator;

    private Integer apiQueries = 0;

    @SneakyThrows

    //TODO: caching?
    public Tinder getAPI() {
        Tinder api = Tinder.fromAccessToken(fb.getToken());
        return api;
    }

    @SneakyThrows
    public List<SyncVerdictResponse> syncVerdictBatch(int size) {
        Tinder api = getAPI();
        List<CrawlerDataDTO> dtos = dao.loadVerdictedButNotSynced(0, size);
        List<SyncVerdictResponse> responses = new ArrayList<>(dtos.size());
        for (CrawlerDataDTO d : dtos) {

            SyncVerdictResponse vsr = syncVerdict(d, api);
            log.info("Sync verdict user: {}, verdict:{}, response: {}", d.getId(), d.getVerdict(), vsr);
            responses.add(vsr);
            TimeUnit.MILLISECONDS.sleep(1000);
        }
        return responses;
    }


    public Integer crawNewData() {
        Set<User> collect = collect(CRAWLER_LOOPS);
        ArrayList<User> details = new ArrayList<>(collect);
        dao.saveBatch(details);
        return scoring();
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
        } else {
            log.info("{} : sync verdict failed", obj);
        }

        SyncVerdictResponse response = new SyncVerdictResponse(success, ret);
        return response;
    }

    public Integer scoring() {
        Integer unratedBefore = dao.countUnrated();

        List<CrawlerDataDTO> users = null;
        while (!(users = dao.loadUnrated()).isEmpty()) {
            Map<String, Integer> ratingMap = new HashMap<>(users.size());
            for (CrawlerDataDTO u : users) {
                int rating = evaluator.evaluate(u);
                ratingMap.put(u.getId(), rating);
            }
            dao.updateRating(ratingMap);
        }
        Integer unratedAfter = dao.countUnrated();
        return unratedBefore - unratedAfter;
    }

    @SneakyThrows
    private Set<User> collect(int loops) {
        Tinder api = getAPI();
        Set<User> uniqueUsers = new HashSet<>();
        for (int i = 0; i < loops; i++) {
            log.info("Crawler recs collect loop: {}/{}", i + 1, loops);
            ArrayList<User> recs;
            try {
                recs = api.getRecommendations();
            } catch (Exception ex) {
                log.error("Exception while recomendation fetch: {}", ex.getClass());
                break;
            }
            Thread.sleep(1000);
            uniqueUsers.addAll(recs);
        }
        return uniqueUsers;
    }
}

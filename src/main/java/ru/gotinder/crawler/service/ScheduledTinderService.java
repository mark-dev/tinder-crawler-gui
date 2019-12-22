package ru.gotinder.crawler.service;

import com.djm.tinder.like.SuperLike;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import ru.gotinder.crawler.common.SyncVerdictResponse;
import ru.gotinder.crawler.persistence.CrawlerDAO;
import ru.gotinder.crawler.persistence.dto.CrawlerDataDTO;
import ru.gotinder.crawler.persistence.dto.VerdictEnum;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ScheduledTinderService {
    private static final Random RANDOM = new Random();

    @Autowired
    private TinderCrawlerService tcs;

    @Autowired
    CrawlerDAO dao;

    @Value("${tinder.crawler.auto-superlike-sync}")
    private Boolean autoSuperLikeSync;

    //TODO: use spring bean
    ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        if (autoSuperLikeSync)
            scheduleSuperLikeSyncAfter(TimeUnit.MINUTES.toSeconds(5));
    }


    private void scheduleSuperLikeSyncAfter(long delayInSec) {
        Instant now = Instant.now();
        log.info("We schedule next auto super like sync at {}", now.plusSeconds(delayInSec));
        scheduledExecutor.schedule(this::scheduledSuperLikeSync, delayInSec, TimeUnit.SECONDS);
    }

    private void scheduledSuperLikeSync() {
        Instant nextSchedule = trySyncAutoLike();
        if (nextSchedule != null) {
            Instant now = Instant.now();
            long delay = Duration.between(now, nextSchedule).getSeconds() + 1;
            if (delay <= 0) {
                delay = 1; // Sync now!
            }
            scheduleSuperLikeSyncAfter(delay);
        } else {
            log.error("trySyncAutoLike() return null! Some tinder-backend exception occurs?");
            scheduleSuperLikeSyncAfter(TimeUnit.MINUTES.toSeconds(30));
        }
    }

    // Пытаемся синхронизировать авто лайк - последний поставленный
    private Instant trySyncAutoLike() {
        int superLikesPerDay = 1;
        List<CrawlerDataDTO> targets = dao.loadSuperLikeCandidates(superLikesPerDay);
        log.info("Auto superlike sync targets is {}", targets);
        List<SyncVerdictResponse> responses = tcs.syncVerdictsBatch(targets);
        Instant nextSchedule = null;

        for (SyncVerdictResponse r : responses) {
            if (r.getTinderResponse() instanceof SuperLike) {
                SuperLike superlike = (SuperLike) r.getTinderResponse();
                Instant resetAt = superlike.getResetAt();
                //Планируем вернуть минимальное время, когда можно заново попробовать синхронизировать суперлайки
                if (nextSchedule == null || nextSchedule.compareTo(resetAt) > 0) {
                    nextSchedule = resetAt;
                }
            } else {
                log.error("Unexpected tinder response {}", r);
            }
        }
        return nextSchedule;
    }


    /*Выкачиваем с тиндера новые рекомендации */
    @Scheduled(cron = "${tinder.crawler.cron}")
    public void sheduleCrawNewData() {
        StopWatch sw = new StopWatch();
        sw.start();

        log.info("sheduleCrawNewData() -- BEGIN");
        Integer ctx = tcs.crawNewData();
        sw.stop();

        log.info("sheduleCrawNewData() -- END. [new: {}, takes: {} ms]", ctx, sw.getLastTaskTimeMillis());
    }

    /*
     * Синхронизируем наши вердикты и вердикты тиндера
     * */
    @Scheduled(cron = "${tinder.crawler.bvsync}")
    public void backgroundVerdictSync() {
        log.debug("Schedule sync verdicts called");
        int limit = 10;

        //TODO: Подумать, насчет порядка, в котором мы синхронизируем вердикты - реалистичнее будет выглядеть, если мы будем случайным образом выбирать, а не order by verdict
        // - В таком случае получается, что мы сначала дизлайкаем потом лайкаем, а это может быть подозрительно - человек себя так не ведет.

        List<SyncVerdictResponse> responses = tcs.syncVerdictedItems(VerdictEnum.LIKE, limit);
        log.debug("Synced {} verdicts in background ", responses.size());
    }


    /*
     * Автоматический лайк определенных пользователей
     * */
    @Scheduled(cron = "${tinder.crawler.autolike}")
    public void autoLike() {
        int maybeMatchCtx = 2 + RANDOM.nextInt(5);
        int likeCandidateCtx = 10 + RANDOM.nextInt(15);

        List<CrawlerDataDTO> likes = new ArrayList<>(maybeMatchCtx + likeCandidateCtx);

        //Лайкаем тех, кого тиндер нам часто показывает
        likes.addAll(dao.loadPossibleLikes(0, maybeMatchCtx));
        //Лайкаем кандидатов на автолайк
        likes.addAll(dao.loadAutoLikeCandidates(0, likeCandidateCtx));

        //TODO: Batch??
        for (CrawlerDataDTO d : likes) {
            dao.setVerdict(d.getId(), VerdictEnum.LIKE, true);
        }

        //TODO: Так-то теоретически могут пересечься два этих множества.. (like и dislike) но считаем что это оч редкая ситуация.

        //Кого-то дизлайкаем (эмулируем реального человека)
        int dislikeCandidateCtx = 5 + RANDOM.nextInt(15);
        List<CrawlerDataDTO> disLikes = new ArrayList<>(dislikeCandidateCtx);
        disLikes.addAll(dao.loadAutoDislikeCandidates(0, dislikeCandidateCtx));

        for (CrawlerDataDTO d : disLikes) {
            dao.setVerdict(d.getId(), VerdictEnum.PASS, true);
        }

        log.info("Autolike finish, liked {} ppl", likes.size());
    }
}

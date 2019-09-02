package ru.gotinder.crawler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import ru.gotinder.crawler.common.SyncVerdictResponse;
import ru.gotinder.crawler.persistence.CrawlerDAO;
import ru.gotinder.crawler.persistence.dto.CrawlerDataDTO;
import ru.gotinder.crawler.persistence.dto.VerdictEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class ScheduledTinderService {
    private static final Random RANDOM = new Random();

    @Autowired
    private TinderCrawlerService tcs;

    @Autowired
    CrawlerDAO dao;

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
        int likeCandidateCtx = 5 + RANDOM.nextInt(15);

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

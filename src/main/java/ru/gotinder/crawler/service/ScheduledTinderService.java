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
        List<SyncVerdictResponse> responses = tcs.syncVerdictedItems(VerdictEnum.LIKE, limit);
        log.debug("Synced {} verdicts in background ", responses.size());
    }


    /*
     * Лайкаем тех, кто вероятно лайкнул нас (часто выдается тиндером)
     * */
    @Scheduled(cron = "${tinder.crawler.autolike}")
    public void autoLike() {
        int limit = 5 + RANDOM.nextInt(15);
        List<CrawlerDataDTO> likes = dao.loadPossibleLikes(0, limit);


        for (CrawlerDataDTO d : likes) {
            dao.setVerdict(d.getId(), VerdictEnum.LIKE);
        }

        log.info("Autolike finish, liked {} ppl", likes.size());
    }
}

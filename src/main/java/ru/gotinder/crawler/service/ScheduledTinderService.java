package ru.gotinder.crawler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import ru.gotinder.crawler.persistence.CrawlerDAO;

@Slf4j
@Service
public class ScheduledTinderService {
    @Autowired
    private TinderCrawlerService tcs;

    @Autowired
    private CrawlerDAO dao;


    public static final long HOUR_MS = 1000L * 60 * 60;

    @Scheduled(fixedRate = HOUR_MS * 2, initialDelay = HOUR_MS)
    public void sheduleCrawNewData() {
        StopWatch sw = new StopWatch();
        sw.start();

        log.info("sheduleCrawNewData() -- BEGIN");
        Integer ctx = tcs.crawNewData();
        sw.stop();

        log.info("sheduleCrawNewData() -- END. [new: {}, takes: {} ms]", ctx, sw.getLastTaskTimeMillis());

    }

    //   @Scheduled(fixedRate = 1000L * 60 * 60 * 24) //раз в 5ч
    public void syncTinderResults() {
        tcs.syncVerdictBatch(50);
    }
}

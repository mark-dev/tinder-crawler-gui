package ru.gotinder.crawler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.gotinder.crawler.persistence.CrawlerDAO;

@Slf4j
@Service
public class ScheduledTinderService {
    @Autowired
    TinderCrawlerService tcs;


    @Autowired
    CrawlerDAO dao;

    public static final long HOUR_MS = 1000L * 60 * 60;

    @Scheduled(fixedRate = HOUR_MS * 2, initialDelay = HOUR_MS)
    public void sheduleCrawNewData() {
        log.info("sheduleCrawNewData -- BEGIN");
        Integer ctx = tcs.crawNewData();
        log.info("scheduled crawler task: {} new entry", ctx);
        log.info("sheduleCrawNewData -- END");
    }

    //   @Scheduled(fixedRate = 1000L * 60 * 60 * 24) //раз в 5ч
    public void syncTinderResults() {
        tcs.syncVerdictBatch(50);
    }
}

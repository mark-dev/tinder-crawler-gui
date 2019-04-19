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

    @Scheduled(fixedRate = 1000L * 60 * 60 * 10) //раз в 10ч
    public void sheduleCrawNewData() {
        log.info("sheduleCrawNewData -- BEGIN");
        Integer ctx = tcs.crawNewData();
        log.info("sheduleCrawNewData -- END");
        log.info("scheduled crawler got {} new values", ctx);
    }

 //   @Scheduled(fixedRate = 1000L * 60 * 60 * 24) //раз в 5ч
    public void syncTinderResults() {
        tcs.syncVerdictBatch(50);
    }
}

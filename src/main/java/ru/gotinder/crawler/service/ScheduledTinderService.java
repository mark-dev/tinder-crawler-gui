package ru.gotinder.crawler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import ru.gotinder.crawler.persistence.CrawlerDAO;
import ru.gotinder.crawler.persistence.dto.CrawlerDataDTO;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScheduledTinderService {
    @Autowired
    private TinderCrawlerService tcs;

    @Autowired
    private CrawlerDAO dao;

    @Autowired
    private ImageCacheService imageCacheService;


    @Scheduled(cron = "${tinder.crawler.cron}")
    public void sheduleCrawNewData() {
        StopWatch sw = new StopWatch();
        sw.start();

        log.info("sheduleCrawNewData() -- BEGIN");
        Integer ctx = tcs.crawNewData();
        sw.stop();

        log.info("sheduleCrawNewData() -- END. [new: {}, takes: {} ms]", ctx, sw.getLastTaskTimeMillis());
    }

    //   @Scheduled(fixedRate = 1000L * 60 * 60 * 24)
    public void syncTinderResults() {
        tcs.syncVerdictBatch(50);
    }


    @Scheduled(fixedRate = 1000 * 60 * 15) //Раз в 15 мин
    public void downloadImages() {
        log.info("downloadImages called");
        StopWatch sw = new StopWatch();
        sw.start();

        //Выгружаем те записи, для которых мы не скачали изображения
        List<CrawlerDataDTO> needImgCacheInit = dao.loadMissInImageCache(100);
        for (CrawlerDataDTO d : needImgCacheInit) {
            List<String> photo = d.getPhoto();
            for (String p : photo) {
                try {
                    URL u = new URL(p);
                    String[] userAndPhotoId = u.getPath().substring(1).split("/");
                    imageCacheService.getImage(userAndPhotoId[0], userAndPhotoId[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //Обновляем флаг, считаем что изображения мы скачали
        List<String> ids = needImgCacheInit.stream()
                .map(CrawlerDataDTO::getId)
                .collect(Collectors.toList());

        if (!ids.isEmpty())
            dao.updateImageCacheDownloadedFlag(ids);

        sw.stop();

        log.info("downloadImages finished, takes {} ms", sw.getLastTaskTimeMillis());
    }
}

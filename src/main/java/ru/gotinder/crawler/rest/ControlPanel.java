package ru.gotinder.crawler.rest;

import com.djm.tinder.like.SuperLikeResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.gotinder.crawler.persistence.CrawlerDAO;
import ru.gotinder.crawler.service.FacebookGateway;
import ru.gotinder.crawler.service.TinderCrawlerService;


@RestController
@RequestMapping("/cp")
@Slf4j
public class ControlPanel {

    @Autowired
    CrawlerDAO dao;


    @Autowired
    FacebookGateway facebookGateway;


    @Autowired
    TinderCrawlerService tcs;

    @GetMapping("/crawler")
    public Integer crawData() {
        return tcs.crawNewData();
    }


    @GetMapping("/superlike")
    @SneakyThrows
    public SuperLikeResponse superLike(@RequestParam("id") String id) {
        SuperLikeResponse res = tcs.getAPI().superLike(id);
        log.info("res: {}", res);
        return res;
    }

    @GetMapping("/sync-verdict")
    @SneakyThrows
    public Object syncVerdict(@RequestParam("id") String id) {
        return tcs.syncVerdict(id);
    }


    @GetMapping("/rescoring")
    public Integer rescoringAll() {
        dao.dropRating();
        return tcs.scoring();
    }
}

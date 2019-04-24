package ru.gotinder.crawler.rest;

import com.djm.tinder.like.SuperLikeResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.gotinder.crawler.persistence.CrawlerDAO;
import ru.gotinder.crawler.persistence.dto.VerdictEnum;
import ru.gotinder.crawler.rest.dto.SetVerdictDTO;
import ru.gotinder.crawler.rest.dto.SyncVerdictDTO;
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

    @PostMapping("/crawler")
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

    @PostMapping("/sync-verdict")
    @SneakyThrows
    public Object syncVerdict(@RequestBody SyncVerdictDTO dto) {
        String id = dto.getId();
        return tcs.syncVerdict(id);
    }

    @PostMapping("/sync-all-verdicts")
    public boolean syncVerdicts() {
        //TODO: warn user if more than
        int limit = 50;
        tcs.syncVerdictBatch(limit);
        //TODO: Return some sync stats (OK/FAILED)
        return true;
    }


    @GetMapping("/rescoring")
    public Integer rescoringAll() {
        dao.dropRating();
        return tcs.scoring();
    }

    @PostMapping("/verdict")
    @SneakyThrows
    public boolean verdict(@RequestBody SetVerdictDTO dto) {
        String id = dto.getId();
        VerdictEnum verdict = dto.getVerdict();
        dao.setVerdict(id, verdict);
        return true;
    }
}

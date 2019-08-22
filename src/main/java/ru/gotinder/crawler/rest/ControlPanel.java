package ru.gotinder.crawler.rest;

import com.djm.tinder.like.Like;
import com.djm.tinder.like.SuperLike;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.gotinder.crawler.common.SyncVerdictResponse;
import ru.gotinder.crawler.persistence.CrawlerDAO;
import ru.gotinder.crawler.persistence.dto.VerdictEnum;
import ru.gotinder.crawler.rest.dto.SetVerdictDTO;
import ru.gotinder.crawler.rest.dto.SyncAllVerdictsDTO;
import ru.gotinder.crawler.rest.dto.SyncVerdictDTO;
import ru.gotinder.crawler.service.FacebookGateway;
import ru.gotinder.crawler.service.TinderCrawlerService;

import java.util.List;


@RestController
@RequestMapping("/cp")
@Slf4j
public class ControlPanel {

    @Autowired
    private CrawlerDAO dao;


    @Autowired
    private FacebookGateway facebookGateway;


    @Autowired
    private TinderCrawlerService tcs;

    @PostMapping("/crawler")
    public Integer crawData() {
        return tcs.crawNewData();
    }

    @PostMapping("/sync-verdict")
    @SneakyThrows
    public Object syncVerdict(@RequestBody SyncVerdictDTO dto) {
        String id = dto.getId();
        return tcs.syncVerdict(id);
    }

    @PostMapping("/sync-all-verdicts")
    public SyncAllVerdictsDTO syncVerdicts() {
        //TODO: Предупреждать если больше накопилось чем limit. Это сделанно чтобы не заспамить запросами
        int limit = 50;
        List<SyncVerdictResponse> responses = tcs.syncVerdictedItems(VerdictEnum.LIKE, limit);

        return calculateSyncVerdictStats(responses);
    }

    @PostMapping("/verdict")
    @SneakyThrows
    public boolean verdict(@RequestBody SetVerdictDTO dto) {
        String id = dto.getId();
        VerdictEnum verdict = dto.getVerdict();
        dao.setVerdict(id, verdict, false);
        return true;
    }

    @PostMapping("/hide/{id}")
    public boolean hide(@PathVariable String id) {
        dao.hide(id);
        return true;
    }

    //TODO: Это чисто для отладки, так-то POST нужно сделать
    @GetMapping("/enrich")
    public Integer rescoringAll() {
        dao.dropEnrichFlag();
        return tcs.enrichData();
    }


    private SyncAllVerdictsDTO calculateSyncVerdictStats(List<SyncVerdictResponse> responses) {
        int success = 0, match = 0, failed = 0;
        for (SyncVerdictResponse r : responses) {
            if (r.isSuccess()) {
                success++;
                boolean isMatch = false;

                if (r.getTinderResponse() instanceof Like) {
                    isMatch = ((Like) r.getTinderResponse()).isMatch();

                } else if (r.getTinderResponse() instanceof SuperLike) {
                    isMatch = ((SuperLike) r.getTinderResponse()).isMatch();
                }

                if (isMatch) {
                    match++;
                }
            } else
                failed++;
        }
        return new SyncAllVerdictsDTO(success, failed, match);
    }
}

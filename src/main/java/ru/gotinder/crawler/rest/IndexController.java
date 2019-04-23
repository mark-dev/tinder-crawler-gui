package ru.gotinder.crawler.rest;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gotinder.crawler.persistence.CrawlerDAO;
import ru.gotinder.crawler.persistence.dto.CrawlerDataDTO;
import ru.gotinder.crawler.persistence.dto.VerdictEnum;
import ru.gotinder.crawler.service.FacebookGateway;
import ru.gotinder.crawler.service.TinderCrawlerService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/")
public class IndexController {


    @Autowired
    CrawlerDAO dao;

    @Autowired
    FacebookGateway facebookGateway;

    @Autowired
    TinderCrawlerService tcs;

    @GetMapping("/")
    public String main(Model model,
                       @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                       @RequestParam(value = "size", required = false, defaultValue = "5") Integer size,
                       @RequestParam(value = "type", required = false, defaultValue = "ALL") CrawlerListTypes type) {
        List<CrawlerDataDTO> users = Collections.emptyList();
        Integer count = 0;
        boolean displaySync = false;
        switch (type) {
            case ALL:
                users = dao.topByRating(page, size);
                count = dao.countTopByRating();
                break;
            case LATEST:
                users = dao.loadLatest(page, size);
                count = dao.countLatest();
                break;
            case VERDICTED:
                users = dao.loadVerdictedButNotSynced(page, size);
                count = dao.countVerdicted();
                displaySync = true;
                break;
            case RECS_DUPLICATED:
                users = dao.loadRecsDuplicated(page, size);
                count = dao.countRecsDuplicated();
                displaySync = true;
                break;
        }
        model.addAttribute("sync", displaySync);
        model.addAttribute("users", users);
        model.addAttribute("count", count);
        model.addAttribute("type", type);
        model.addAttribute("size", size);
        model.addAttribute("page", page);
        return "index";
    }

    @GetMapping("/verdict")
    @SneakyThrows
    public String verdict(@RequestParam("id") String id, @RequestParam("verdict") VerdictEnum verdict) {
        dao.setVerdict(id, verdict);
        return "redirect:/";
    }

    @GetMapping("/refresh-token")
    public String refreshToken() {
        facebookGateway.refreshToken();
        return "redirect:/";
    }

    @GetMapping("/user/{id}")
    public String byId(@PathVariable String id, Model model) {
        Optional<CrawlerDataDTO> u = dao.byId(id);
        List<CrawlerDataDTO> payload = u.map((d) -> Arrays.asList(d)).orElse(Collections.emptyList());
        model.addAttribute("users", payload);
        model.addAttribute("sync", true);
        return "index";
    }

    @GetMapping("/sync-verdicts")
    public String syncVerdicts() {
        tcs.syncVerdictBatch(50);
        return "redirect:/";
    }
}

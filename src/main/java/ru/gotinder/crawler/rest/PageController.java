package ru.gotinder.crawler.rest;

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
public class PageController {


    @Autowired
    CrawlerDAO dao;

    @Autowired
    FacebookGateway facebookGateway;

    @Autowired
    TinderCrawlerService tcs;


    @GetMapping({"/"})
    public String latest(Model model,
                         @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                         @RequestParam(value = "size", required = false, defaultValue = "5") Integer size) {
        List<CrawlerDataDTO> users = dao.loadLatest(page, size);
        Integer count = dao.countLatest();
        model.addAttribute("users", users);
        model.addAttribute("count", count);
        model.addAttribute("size", size);
        model.addAttribute("page", page);

        return "latest";
    }

    @GetMapping({"random"})
    public String random(Model model) {
        int loadCtx = 4;
        List<CrawlerDataDTO> users = dao.loadRandom(loadCtx);
        model.addAttribute("users", users);
        model.addAttribute("count", loadCtx);
        model.addAttribute("size", loadCtx);
        model.addAttribute("page", 0);

        return "random";
    }

    @GetMapping({"todays"})
    public String todays(Model model,
                         @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                         @RequestParam(value = "size", required = false, defaultValue = "5") Integer size) {
        List<CrawlerDataDTO> users = dao.loadTodays(page, size);
        Integer count = dao.countTodays();
        model.addAttribute("users", users);
        model.addAttribute("count", count);
        model.addAttribute("size", size);
        model.addAttribute("page", page);

        return "todays";
    }

    @GetMapping({"near"})
    public String near(Model model,
                       @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                       @RequestParam(value = "size", required = false, defaultValue = "5") Integer size) {
        List<CrawlerDataDTO> users = dao.loadNear(page, size);
        Integer count = dao.countNear();
        model.addAttribute("users", users);
        model.addAttribute("count", count);
        model.addAttribute("size", size);
        model.addAttribute("page", page);

        return "near";
    }

    @GetMapping({"verdicted"})
    public String verdicted(Model model,
                            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                            @RequestParam(value = "size", required = false, defaultValue = "5") Integer size) {
        List<CrawlerDataDTO> users = dao.loadVerdictedButNotSynced(VerdictEnum.SUPERLIKE, page, size);
        Integer count = dao.countVerdicted();
        model.addAttribute("users", users);
        model.addAttribute("count", count);
        model.addAttribute("size", size);
        model.addAttribute("page", page);

        return "verdicted";
    }

    @GetMapping({"likes"})
    public String likes(Model model,
                        @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                        @RequestParam(value = "size", required = false, defaultValue = "5") Integer size) {
        List<CrawlerDataDTO> users = dao.loadPossibleLikes(page, size);
        Integer count = dao.countPossibleLikes();
        model.addAttribute("users", users);
        model.addAttribute("count", count);
        model.addAttribute("size", size);
        model.addAttribute("page", page);

        return "likes";
    }

    @GetMapping("search")
    public String search(Model model,
                         @RequestParam(value = "q") String q,
                         @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                         @RequestParam(value = "size", required = false, defaultValue = "5") Integer size) {
        List<CrawlerDataDTO> users = dao.search(q, page, size);

        Integer count = dao.countSearch(q);

        model.addAttribute("users", users);
        model.addAttribute("count", count);
        model.addAttribute("size", size);
        model.addAttribute("page", page);
        model.addAttribute("search", q);
        return "search";
    }

    @GetMapping({"top"})
    public String index(Model model,
                        @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                        @RequestParam(value = "size", required = false, defaultValue = "5") Integer size) {

        List<CrawlerDataDTO> users = dao.topByRating(page, size);

        Integer count = dao.countTopByRating();

        model.addAttribute("users", users);
        model.addAttribute("count", count);
        model.addAttribute("size", size);
        model.addAttribute("page", page);
        return "top";
    }

    @GetMapping("/user/{id}")
    public String byId(@PathVariable String id, Model model) {
        Optional<CrawlerDataDTO> u = dao.byId(id);
        List<CrawlerDataDTO> payload = u.map((d) -> Arrays.asList(d)).orElse(Collections.emptyList());
        model.addAttribute("users", payload);
        model.addAttribute("sync", true);
        return "top";
    }

}

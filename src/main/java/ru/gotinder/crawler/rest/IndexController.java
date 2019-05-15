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


    @GetMapping({"latest"})
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

    @GetMapping({"verdicted"})
    public String verdicted(Model model,
                            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                            @RequestParam(value = "size", required = false, defaultValue = "5") Integer size) {
        List<CrawlerDataDTO> users = dao.loadVerdictedButNotSynced(page, size);
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

    @GetMapping({"/"})
    @SneakyThrows
    public String index(Model model,
                        @RequestParam(value = "q", required = false, defaultValue = "") String search,
                        @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                        @RequestParam(value = "size", required = false, defaultValue = "5") Integer size) {

        List<CrawlerDataDTO> users = dao.topByRating(search, page, size);
        //TODO: search сюда передаем еще.
        //TODO: Пора юзать Spring Data и Specifications..

        Integer count = dao.countTopByRating(search);

        model.addAttribute("users", users);
        model.addAttribute("count", count);
        model.addAttribute("size", size);
        model.addAttribute("page", page);
        return "index";
    }

    @GetMapping("/user/{id}")
    public String byId(@PathVariable String id, Model model) {
        Optional<CrawlerDataDTO> u = dao.byId(id);
        List<CrawlerDataDTO> payload = u.map((d) -> Arrays.asList(d)).orElse(Collections.emptyList());
        model.addAttribute("users", payload);
        model.addAttribute("sync", true);
        return "index";
    }

}

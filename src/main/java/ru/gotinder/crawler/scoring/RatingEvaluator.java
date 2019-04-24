package ru.gotinder.crawler.scoring;

import org.springframework.stereotype.Service;
import ru.gotinder.crawler.persistence.dto.CrawlerDataDTO;

import java.util.HashMap;
import java.util.Map;

@Service
public class RatingEvaluator {
    private static final Map<String, Integer> KEYWORDS = new HashMap<String, Integer>() {{
        put("лейбол", 50);
        put("volley", 50);
        put("шахматы", 50);
        put("вело", 50);
        put("кот", 10);
        put("аналитическ", 30);
        put("зануда", 20);
        put("обычн", 10);
        put("программи", 30);
        put("адекватн", 10);
        put("спокойн", 10);
        put("танцы", 20);
        put("танцами", 20);
        put("буги", 30);
        put("свинг", 30);
        put("ролл", 30); //рок-н-ролл
        put("дева", 10);
        put("честнос", 10);
        put("доска", 10);
        put("борд", 10);
        put("лонгборд", 50);
        put("скейт", 50);
        put("серф", 30);
        put("спорт", 10);
        put("бег", 20);
        put("сарказм", 10);
        put("самоирония", 10);
        put("черный", 10);
        put("джаз", 10);
        put("работ", 10);
        put("универ", 10);
        put("учеба", 10);
        put("игры", 10);
        put("геймер", 10);
        put("айти", 10);
        put("IT", 10);
        put("гуля",10); //гулять гуляем итп
        put("интроверт",10);
        put("двач",15);

        put("сын", -1000);
        put("ребенок", -1000);
        put("ребёнок", -1000);
        put("дочь", -1000);
        put("дочка", -1000);
        put("тату", -1000);
        put("симпатичная", -1000);
        put("умная", -1000);
        put("красивая", -1000);
        put("420", -1000);

    }};

    public int evaluate(CrawlerDataDTO u) {
        String bio = u.getBio().toLowerCase();

        if (bio.isEmpty()) return 0;

        int rating = 0;
        //TODO: Алгоритм придумать из компьютер сайенс ченить на тему поиска, префиксных деревьев итп
        //https://ru.wikipedia.org/wiki/%D0%90%D0%BB%D0%B3%D0%BE%D1%80%D0%B8%D1%82%D0%BC_%D0%90%D1%85%D0%BE_%E2%80%94_%D0%9A%D0%BE%D1%80%D0%B0%D1%81%D0%B8%D0%BA
        for (Map.Entry<String, Integer> e : KEYWORDS.entrySet()) {
            if (bio.contains(e.getKey())) {
                rating += e.getValue();
            }
        }
        return rating;
    }

}

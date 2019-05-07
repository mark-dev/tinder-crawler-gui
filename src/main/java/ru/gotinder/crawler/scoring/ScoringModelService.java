package ru.gotinder.crawler.scoring;

import org.springframework.stereotype.Service;
import ru.gotinder.crawler.persistence.dto.CrawlerDataDTO;

import java.util.HashMap;
import java.util.Map;

@Service
public class ScoringModelService {
    private static final Map<String, Integer> KEYWORDS = new HashMap<String, Integer>() {{
        //хобби:

        put("лейбол", 50);
        put("volley", 50);
        put("шахматы", 50);
        put("велосипед", 50);
        put("лонгборд", 50);
        put("скейт", 50);
        put("буги", 30);

        put("танц", 20); //танцы
        put("доска", 10);
        put("борд", 10);
        put("серф", 30);
        put("спорт", 10);
        put("бег", 20);
        put("игры", 10);
        put("геймер", 10);

        //Музыка
        put("свинг", 30);
        put("ролл", 30); //рок-н-ролл
        put("джаз", 10);

        //Айти
        put("программи", 30);
        put("аналитическ", 30);
        put("кот", 10);
        put("IT", 10);
        put("айти", 10);

        //Качества
        put("зануда", 20);
        put("обычн", 10);
        put("адекватн", 10);
        put("спокойн", 10);
        put("честнос", 10);
        put("сарказм", 10);
        put("самоирония", 10);
        put("черный", 10);
        put("интроверт",10);

        //Зодиак
        put("дева", 10);

        //Иные занятия
        put("работ", 10);
        put("универ", 10);
        put("учеба", 10);

        //Прочее
        put("гуля",10); //гулять гуляем итп
        put("двач",15); //двач сила
        put("давай", 30); //предложение чето сделать
        put("пойдем", 30); //предложение чето сделать
        put("пойдём", 30); //предложение чето сделать

        //Нафиг
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
        //TODO: Можно использовать какой-нить Алгоритм из компьютер сайенс ченить на тему поиска, префиксных деревьев итп
        //https://ru.wikipedia.org/wiki/%D0%90%D0%BB%D0%B3%D0%BE%D1%80%D0%B8%D1%82%D0%BC_%D0%90%D1%85%D0%BE_%E2%80%94_%D0%9A%D0%BE%D1%80%D0%B0%D1%81%D0%B8%D0%BA

        //TODO: По хорошему бред конечно таким образом это проверять, т.к. важно учитывать отношение к слову.
        //TODO: Иначе может написать, ненавижу программистов катающихся на велосипеде, а мы большой рейтинг дадим.
        //TODO: А это задача для NLP. А это заниматься нужно, отдельный человек нужен (с)
        //TODO: Тоесть видимо нужно сначала извлечь слова которые нас интересуют? задача NER
        //TODO: Дальше как-то понять, поощряется ли это в тексте или нет. Видимо тут, на СЗ, если нет негативных модификаторов, считаем что поощряется

        for (Map.Entry<String, Integer> e : KEYWORDS.entrySet()) {
            if (bio.contains(e.getKey())) {
                rating += e.getValue();
            }
        }
        return rating;
    }

}

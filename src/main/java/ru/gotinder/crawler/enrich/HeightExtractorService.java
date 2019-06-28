package ru.gotinder.crawler.enrich;

import org.springframework.stereotype.Service;
import ru.gotinder.crawler.persistence.dto.CrawlerDataDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HeightExtractorService {

    //https://stackoverflow.com/questions/7317043/regex-not-operator/7317087
    //Exactly 3 digits(not 4,5)
    private static final Pattern HEIGHT_EXTRACT_PATTERN = Pattern.compile("(?<![0-9|a-z])([0-9]{3}|[0-9]\\.\\d{2})(?![0-9])");


    private static final int MAX_HEIGHT = 210;
    private static final int MIN_HEIGHT = 140;


    public Integer extractHeight(CrawlerDataDTO dto) {
        Integer height = -1;

        Matcher matcher = HEIGHT_EXTRACT_PATTERN.matcher(dto.getBio());
        List<Integer> digits = new ArrayList<>();

        while (matcher.find()) {
            String group = matcher.group(1).replaceAll("[^\\d]", "");
            int probableHeight = Integer.parseInt(group);
            if (probableHeight >= MIN_HEIGHT && probableHeight <= MAX_HEIGHT)
                digits.add(probableHeight);
        }

        if (!digits.isEmpty()) {
            digits.sort(Integer::compareTo);
            return digits.get(0);
        }

        return height;
    }
}

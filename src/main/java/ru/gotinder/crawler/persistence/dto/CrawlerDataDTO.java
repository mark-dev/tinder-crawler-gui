package ru.gotinder.crawler.persistence.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@ToString(of = {"id","name","verdict"})
public class CrawlerDataDTO {

    private String id;
    private String name;
    private List<String> photo;
    private String bio;
    private Integer rating;
    private Integer distance;
    private LocalDate birthday;

    private Instant ts;
    private Instant updatedAt;
    private VerdictEnum verdict;
    private boolean vedictSync;

    //System stuff
    private String contentHash;
    private String sNumber;
    private Integer recsDuplicateCount;


}

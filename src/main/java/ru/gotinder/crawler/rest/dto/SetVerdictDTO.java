package ru.gotinder.crawler.rest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gotinder.crawler.persistence.dto.VerdictEnum;

@Data
@NoArgsConstructor
public class SetVerdictDTO {
    private String id;
    private VerdictEnum verdict;
}

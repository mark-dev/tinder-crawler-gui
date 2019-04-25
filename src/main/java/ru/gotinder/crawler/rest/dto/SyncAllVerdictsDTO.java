package ru.gotinder.crawler.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SyncAllVerdictsDTO {
    private int success;
    private int failed;
    private int match;
}

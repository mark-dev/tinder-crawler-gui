package ru.gotinder.crawler.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncVerdictResponse {
    private boolean success;
    private Object tinderResponse;
}

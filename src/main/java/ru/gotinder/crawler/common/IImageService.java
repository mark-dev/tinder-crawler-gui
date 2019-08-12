package ru.gotinder.crawler.common;

import java.io.InputStream;
import java.util.Optional;

public interface IImageService {
    Optional<InputStream> getImage(String userId, String imageId);
}

package ru.gotinder.crawler.common;

import java.io.InputStream;
import java.util.Optional;

public interface IImageService {
    String TINDER_IMG_URL = "https://images-ssl.gotinder.com";

    Optional<InputStream> getImage(String userId, String imageId);

    default boolean canUseRedirect() {
        return false;
    }

    default String getTinderImgURL(String userId, String imageId) {
        return TINDER_IMG_URL + "/" + userId + "/" + imageId;
    }
}

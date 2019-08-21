package ru.gotinder.crawler.service;

import lombok.extern.slf4j.Slf4j;
import ru.gotinder.crawler.common.IImageService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

@Slf4j
public class SimpleImageService implements IImageService {


    @Override
    public Optional<InputStream> getImage(String userId, String imageId) {
        String tinderRequestUrl = getTinderImgURL(userId, imageId);
        try {
            URL u = new URL(tinderRequestUrl);
            return Optional.of(u.openStream());
        } catch (IOException ex) {
            return Optional.empty();
        }
    }

    @Override
    public boolean canUseRedirect() {
        return true;
    }
}

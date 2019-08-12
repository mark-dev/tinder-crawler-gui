package ru.gotinder.crawler.cfg;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.gotinder.crawler.common.IImageService;
import ru.gotinder.crawler.service.ImageCacheService;
import ru.gotinder.crawler.service.SimpleImageService;

@Configuration
public class ImageServiceConfiguration {


    @Bean
    @ConditionalOnProperty(value = "tinder.img-cache.enabled", havingValue = "true")
    public IImageService cachingImageService() {
        return new ImageCacheService();
    }

    @Bean
    @ConditionalOnMissingBean(IImageService.class)
    public IImageService defaultImageService() {
        return new SimpleImageService();
    }
}

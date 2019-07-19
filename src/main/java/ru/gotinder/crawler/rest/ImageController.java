package ru.gotinder.crawler.rest;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gotinder.crawler.service.ImageCacheService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@RestController
@RequestMapping("/image")
public class ImageController {

    @Autowired
    ImageCacheService service;


    @GetMapping(value = "/")
    public String test() {
        return "abc";
    }

    @GetMapping(value = "/{userId}/{imageId}")
    public void getImageAsByteArray(HttpServletResponse response, @PathVariable("userId") String userId, @PathVariable("imageId") String imageId) throws IOException {
        Optional<InputStream> image = service.getImage(userId, imageId);
        if (image.isPresent()) {
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            IOUtils.copy(image.get(), response.getOutputStream());
        }
    }
}

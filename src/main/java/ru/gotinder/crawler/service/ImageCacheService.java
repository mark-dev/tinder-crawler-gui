package ru.gotinder.crawler.service;

//Проблема в том, что изображение перестает быть доступным по ссылке, если пользователь его удалил

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

@Service
@Slf4j
public class ImageCacheService {

    private final String TINDER_IMG_URL = "https://images-ssl.gotinder.com";
    private final String CACHE_DIR = "/media/mark/DATAPART1/img-cache";


    private File cacheDirFile;

    @PostConstruct
    public void init() {
        cacheDirFile = new File(CACHE_DIR);
        if (!cacheDirFile.exists()) {
            boolean ret = cacheDirFile.mkdir();
            if (!ret)
                throw new RuntimeException("Failed to create image cache directory");
        }
    }

    //Сюда передается кусок из URL, первое это userId, второе - photoId /5d0c9fa2613788150008ca8c/original_332477c6-e2aa-4f90-850b-fe1c289d47bb.jpeg
    public Optional<InputStream> getImage(String userId, String imageId) {

        File f = getRelatedFileInCache(userId, imageId);
        if (!f.exists()) {
            Optional<BufferedImage> loadedFromTinder = loadFromTinderBackend(userId, imageId);
            if (loadedFromTinder.isPresent()) {
                BufferedImage is = loadedFromTinder.get();

                byte[] bytes = saveImageToFile(is, f, userId);
                return Optional.of(new ByteArrayInputStream(bytes));
            } else {
                log.error("seems {}/{} is undownloadable", userId, imageId);
                return Optional.empty();
                //TODO: пометить что этот файл неудалось скачать и в дальнейшем не пытаться

            }
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(f);
                return Optional.of(fileInputStream);
            } catch (FileNotFoundException e) {
                return Optional.empty();
            }
        }

    }


    private String buildRequestURL(String userId, String photoId) {
        return TINDER_IMG_URL + "/" + userId + "/" + photoId;
    }

    private File getRelatedFileInCache(String userId, String photoId) {
        return Paths.get(CACHE_DIR, userId, photoId).toFile();
    }

    private File getRelatedFileInCache(String userId) {
        return Paths.get(CACHE_DIR, userId).toFile();
    }

    private Optional<BufferedImage> loadFromTinderBackend(String userId, String photoId) {
        String tinderRequestUrl = buildRequestURL(userId, photoId);
        try {
            URL u = new URL(tinderRequestUrl);
            return Optional.ofNullable(ImageIO.read(u));
        } catch (IOException ex) {
            //Не удалось выгрузить, 403 ошибка например будет если файл удален
            log.debug("tinder img download exception", ex);
            return Optional.empty();
        }
    }

    private byte[] readInputStreamAsBytes(InputStream is) {
        try {
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            return buffer;
        } catch (IOException ex) {
            return new byte[]{};
        }
    }

    private byte[] saveImageToFile(BufferedImage is, File file, String userId) {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(is, "jpg", baos);
            byte[] bytes = baos.toByteArray();

            File userCacheDirectory = getRelatedFileInCache(userId);
            if (!userCacheDirectory.exists()) {
                boolean success = userCacheDirectory.mkdir();
                if (!success)
                    throw new RuntimeException("Failed to create user image cache directory, userId: " + userId);
            }

            if (!file.exists()) {
                boolean createFileRes = file.createNewFile();
                if (!createFileRes)
                    throw new RuntimeException("Failed to create image file in cache " + file.getAbsolutePath());
            }
            Files.write(file.toPath(), bytes);
            return bytes;
        } catch (IOException ex) {
            //TODO: обработать, вообще говоря не реальная ситуация
            throw new RuntimeException(ex);
        }
    }
}

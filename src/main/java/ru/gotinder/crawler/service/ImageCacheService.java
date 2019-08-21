package ru.gotinder.crawler.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StopWatch;
import ru.gotinder.crawler.persistence.CrawlerDAO;
import ru.gotinder.crawler.persistence.dto.CrawlerDataDTO;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


//Кеширует изображения в локальной директории
//Проблема в том, что изображение перестает быть доступным по ссылке, если пользователь его удалил
//Но и объем места, необходимый для такого кеша оч велик. Как вариант - кешировать только пользователей с хорошим рейтингом
@Slf4j
public class ImageCacheService extends SimpleImageService {

    @Autowired
    private CrawlerDAO dao;

    @Value("${tinder.img-cache.dir}")
    private String cacheDir;


    private File cacheDirFile;

    @PostConstruct
    public void init() {
        cacheDirFile = new File(cacheDir);
        if (!cacheDirFile.exists()) {
            boolean ret = cacheDirFile.mkdir();
            if (!ret)
                throw new RuntimeException("Failed to create image cache directory");
        }
    }

    //Сюда передается кусок из URL, первое это userId, второе - photoId /5d0c9fa2613788150008ca8c/original_332477c6-e2aa-4f90-850b-fe1c289d47bb.jpeg
    @Override
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

    @Scheduled(fixedRate = 1000 * 60 * 15) //Раз в 15 мин
    public void downloadImages() {
        log.info("downloadImages called");
        StopWatch sw = new StopWatch();
        sw.start();

        //Выгружаем те записи, для которых мы не скачали изображения
        List<CrawlerDataDTO> needImgCacheInit = dao.loadMissInImageCache(100);
        for (CrawlerDataDTO d : needImgCacheInit) {
            List<String> photo = d.getPhoto();
            for (String p : photo) {
                try {
                    URL u = new URL(p);
                    String[] userAndPhotoId = u.getPath().substring(1).split("/");
                    getImage(userAndPhotoId[0], userAndPhotoId[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //Обновляем флаг, считаем что изображения мы скачали
        List<String> ids = needImgCacheInit.stream()
                .map(CrawlerDataDTO::getId)
                .collect(Collectors.toList());

        if (!ids.isEmpty())
            dao.updateImageCacheDownloadedFlag(ids);

        sw.stop();

        log.info("downloadImages finished, takes {} ms", sw.getLastTaskTimeMillis());
    }

    protected Optional<BufferedImage> loadFromTinderBackend(String userId, String photoId) {
        String tinderRequestUrl = getTinderImgURL(userId, photoId);
        try {
            URL u = new URL(tinderRequestUrl);
            return Optional.ofNullable(ImageIO.read(u));
        } catch (IOException ex) {
            //Не удалось выгрузить, 403 ошибка например будет если файл удален
            log.debug("tinder img download exception", ex);
            return Optional.empty();
        }
    }

    private File getRelatedFileInCache(String userId, String photoId) {
        return Paths.get(cacheDir, userId, photoId).toFile();
    }

    private File getRelatedFileInCache(String userId) {
        return Paths.get(cacheDir, userId).toFile();
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

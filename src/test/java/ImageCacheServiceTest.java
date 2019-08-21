import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.gotinder.crawler.service.ImageCacheService;


//https://www.youtube.com/watch?v=FZ-feRdu6uk
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ImageCacheService.class)
public class ImageCacheServiceTest {
    @Autowired
    ImageCacheService service;

    @Test
    public void test() {
        String userId = "5b9c783a8145f2de6e0cd4df";
        String imageId = "1080x1350_5a5b8017-d03c-442b-94f0-668972c89412.jpg";
        service.getImage(userId, imageId);
        service.getImage(userId, imageId);
        service.getImage(userId, imageId);
        service.getImage(userId, imageId);
    }
}

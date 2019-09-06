import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import ru.gotinder.crawler.service.FacebookGateway;


//https://www.youtube.com/watch?v=FZ-feRdu6uk
@RunWith(SpringRunner.class)
@SpringBootTest(classes = FacebookGateway.class)
public class FacebookGatewayTest {
    @Autowired
    FacebookGateway fb;

    @Test
    public void test() {
        String token = fb.getToken();
        System.out.println(token);
    }
}

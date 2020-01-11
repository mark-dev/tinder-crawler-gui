package ru.gotinder.crawler.service;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.gotinder.crawler.common.IFacebookService;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@ConditionalOnProperty(value = "tinder.crawler.fb.enabled", havingValue = "true")
public class FacebookGateway implements IFacebookService {
    private static Pattern EXTRACT_TOKEN_PATTERN = Pattern.compile("access_token=([\\w\\d]+)");

    @Value("${tinder.crawler.fb.login-url}")
    private String facebookLoginUrl;

    @Value("${tinder.crawler.fb.login}")
    private String fbLogin;

    @Value("${tinder.crawler.fb.password}")
    private String fbPass;

    @Value("${tinder.crawler.fb.token-url}")
    private String facebookTinderSignInUrl;

    @Value("${tinder.crawler.fb.chrome-path}")
    private String chromePath;


    private String token;
    private Instant lastTokenTs = null;

    private WebDriver driver;
    private ChromeOptions settings;

    @PostConstruct
    public void init() {

        settings = new ChromeOptions();
        settings.setBinary(chromePath);
        settings.addArguments("--headless");
        settings.addArguments("--no-sandbox");
        settings.addArguments("--disable-gpu");
        settings.addArguments("--disable-dev-shm-usage");
        //На старте авторизируемся в фейсбуке
        //TODO: Делать это по таймеру, может протухнуть сессия
        //TODO: Сделать ленивую загрузку, на старте это не нужно
        driver = loginToFacebook();
    }

    public synchronized String getToken() {
        if (!hasValidToken()) {
            refreshToken();
        }
        return token;
    }

    private WebDriver loginToFacebook() {
        try {
            WebDriver driver = new ChromeDriver(settings);
            driver.get(facebookLoginUrl);

            WebElement login = driver.findElement(By.id("email"));
            WebElement password = driver.findElement(By.id("pass"));
            login.sendKeys(fbLogin);
            password.sendKeys(fbPass);

            driver.findElement(By.id("loginbutton")).submit();
            return driver;
        } catch (Exception ex) {
            log.info("Exception while login to facebook", ex);
            System.exit(1);
        }
        throw new RuntimeException("Not reachable");
    }

    //TODO: Нужно убедится что авторизационная сессия с фейсбуком не истекла
    private String tinderSignInViaFacebookAndGetToken() {
        try {
            driver.get(facebookTinderSignInUrl);
            WebElement element = driver.findElement(By.xpath("//*[@id=\"platformDialogForm\"]/div[2]/table/tbody/tr/td[1]/table/tbody/tr/td[2]/button[2]"));
            element.submit();

            String htmlSource = driver.getPageSource();
            Matcher matcher = EXTRACT_TOKEN_PATTERN.matcher(htmlSource);
            boolean hasToken = matcher.find();

            if (hasToken) {
                return matcher.group(1);
            } else
                throw new RuntimeException("No token found after submit tinder-app sign-in form");
        } catch (Exception ex) {
            log.error("Exeption while facebook auth", ex);
            System.exit(1);
        }

        throw new RuntimeException("cannot reach here");
    }

    private boolean hasValidToken() {
        return lastTokenTs != null && Duration.between(lastTokenTs, Instant.now()).compareTo(Duration.ofHours(1)) < 0;
    }

    private void refreshToken() {
        token = tinderSignInViaFacebookAndGetToken();
        lastTokenTs = Instant.now();
    }
}

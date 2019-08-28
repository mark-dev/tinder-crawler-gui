package ru.gotinder.crawler.service;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FacebookGateway {
    private static Pattern EXTRACT_TOKEN_PATTERN = Pattern.compile("access_token=([\\w\\d]+)");

    @Value("${tinder.crawler.fb.login-url}")
    private String facebookLoginUrl;

    @Value("${tinder.crawler.fb.login}")
    private String fbLogin;

    @Value("${tinder.crawler.fb.password}")
    private String fbPass;

    @Value("${tinder.crawler.fb.token-url}")
    private String facebookTinderSignInUrl;

    @Value("${tinder.crawler.fb.driver-path}")
    private String driverPath;


    private String token;
    private Instant lastTokenTs = null;

    private WebDriver driver;
    private DesiredCapabilities settings;

    @PostConstruct
    public void init() {

        settings = new DesiredCapabilities();
        settings.setJavascriptEnabled(true);
        settings.setCapability("takesScreenshot", false);
        settings.setCapability("userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
        settings.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, driverPath);

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
            WebDriver driver = new PhantomJSDriver(settings);
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

package ru.gotinder.crawler.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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

    @Value("${tinder.crawler.facebook-token-url}")
    private String tokenUrl;

    @Value("${tinder.crawler.chrome-profile-dir}")
    private String chromeProfileDir;

    private String token;
    private Instant lastTokenTs = null;
    private ChromeOptions chromeOptions;

    @PostConstruct
    public void init() {
        chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("user-data-dir=" + chromeProfileDir);
        chromeOptions.addArguments("--start-maximized");
    }

    public synchronized boolean hasToken() {
        return lastTokenTs != null && Duration.between(lastTokenTs, Instant.now()).compareTo(Duration.ofHours(1)) < 0;
    }

    public synchronized String getToken() {
        if (!hasToken()) {
            refreshToken();
        }
        return token;
    }

    public synchronized void refreshToken() {
        token = obtainFaceBookTokenViaChromeDriver();
        lastTokenTs = Instant.now();
    }

    @SneakyThrows
    private String obtainFaceBookTokenViaChromeDriver() {
        WebDriver driver = new ChromeDriver(chromeOptions);
        driver.get(tokenUrl);
        try {
            WebElement element = driver.findElement(By.xpath("//*[@id=\"platformDialogForm\"]/div[2]/table/tbody/tr/td[1]/table/tbody/tr/td[2]/button[2]"));
            element.submit();
        } catch (NoSuchElementException ex) {
            log.info("Facebook submit form not found \n" +
                    "Probably this is your first application run, you need manually login to facebook using special selenium profile of google chrome \n" +
                    "Sign in and restart app");
            //TODO: implement login via hard-coded login&password
            System.exit(0);
        }


        String htmlSource = driver.getPageSource();

        log.debug(htmlSource);
        Matcher matcher = EXTRACT_TOKEN_PATTERN.matcher(htmlSource);
        boolean hasToken = matcher.find();

        driver.quit();

        if (hasToken) {
            return matcher.group(1);
        } else
            return null;
    }


}

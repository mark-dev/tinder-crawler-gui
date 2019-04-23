package ru.gotinder.crawler.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FacebookGateway {
    private static Pattern EXTRACT_TOKEN_PATTERN = Pattern.compile("access_token=([\\w\\d]+)");

    private String token;
    private Instant lastTokenTs = null;

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

        String profileDir = "/home/mark/.config/google-chrome/selenium";
        String tokenUrl = "https://www.facebook.com/v2.6/dialog/oauth?redirect_uri=fb464891386855067%3A%2F%2Fauthorize%2F&scope=user_birthday%2Cuser_photos%2Cuser_education_history%2Cemail%2Cuser_relationship_details%2Cuser_friends%2Cuser_work_history%2Cuser_likes&response_type=token%2Csigned_request&client_id=464891386855067";

        System.setProperty("webdriver.chrome.driver", "/home/mark/.bin/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-data-dir=" + profileDir);
        options.addArguments("--start-maximized");
        options.setBinary("/usr/bin/google-chrome");
        WebDriver driver = new ChromeDriver(options);
        driver.get(tokenUrl);
        try {
            WebElement element = driver.findElement(By.xpath("//*[@id=\"platformDialogForm\"]/div[2]/table/tbody/tr/td[1]/table/tbody/tr/td[2]/button[2]"));
            element.submit();
        } catch (NoSuchElementException ex) {
            //TODO: implement login via hard-coded login&password
            log.info("Facebook submit form not found \n" +
                    "Probably this is your first application run, you need manually login to facebook using selenium profile of google chrome \n" +
                    "Sign in and restart app");
            Thread.sleep(10000);
        }


        String htmlSource = driver.getPageSource();

        log.debug(htmlSource);
        Matcher matcher = EXTRACT_TOKEN_PATTERN.matcher(htmlSource);
        boolean hasToken = matcher.find();

        driver.quit();

        if (hasToken) {
            String token = matcher.group(1);
            return token;
        } else
            return null;
    }


}

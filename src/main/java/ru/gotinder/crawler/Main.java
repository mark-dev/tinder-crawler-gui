package ru.gotinder.crawler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class Main implements CommandLineRunner {


    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }


    @Override
    public void run(String... args) throws Exception {

    }
}

package com.booknook.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BooknookBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BooknookBackendApplication.class, args);
    }
}

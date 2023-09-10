package com.ohc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CheckinGladosApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheckinGladosApplication.class, args);
    }

}

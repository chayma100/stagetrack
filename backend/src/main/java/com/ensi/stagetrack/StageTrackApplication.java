package com.ensi.stagetrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class StageTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(StageTrackApplication.class, args);
    }
}

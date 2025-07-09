package com.moddynerd.videoprocessingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class VideoProcessingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoProcessingServiceApplication.class, args);
    }

}

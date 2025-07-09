package com.moddynerd.videoprocessingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "video-service")
//@FeignClient(name = "video-service", url = "http://localhost:8080")
public interface VideoServiceClient {

    @PutMapping("/api/videos/{videoId}/status")
    void updateVideoStatus(@PathVariable String videoId,
                           @RequestParam String status);

    @PutMapping("/api/videos/{videoId}/status-with-error")
    void updateVideoStatusWithError(@PathVariable String videoId,
                                    @RequestParam String status,
                                    @RequestParam String errorMessage);

    @PutMapping("/api/videos/{videoId}/duration")
    void updateVideoDuration(@PathVariable String videoId,
                             @RequestParam String duration);
}

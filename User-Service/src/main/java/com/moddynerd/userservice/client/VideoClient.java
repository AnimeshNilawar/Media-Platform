package com.moddynerd.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "video-service")
public interface VideoClient {

    @GetMapping("/video/check/{videoId}")
    boolean checkVideoExists(@PathVariable String videoId);

    @GetMapping("/video/{userId}/videos")
    ResponseEntity<?> getUserVideos(@PathVariable String userId);
}

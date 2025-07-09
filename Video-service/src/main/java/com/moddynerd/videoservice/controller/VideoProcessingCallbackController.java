package com.moddynerd.videoservice.controller;

import com.moddynerd.videoservice.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
public class VideoProcessingCallbackController {

    @Autowired
    VideoService videoService;

    // Endpoints for video processing service communication
    @PutMapping("/{videoId}/status")
    public ResponseEntity<String> updateVideoStatus(@PathVariable String videoId,
                                                   @RequestParam String status) {
        return videoService.updateVideoStatus(videoId, status);
    }

    @PutMapping("/{videoId}/status-with-error")
    public ResponseEntity<String> updateVideoStatusWithError(@PathVariable String videoId,
                                                            @RequestParam String status,
                                                            @RequestParam String errorMessage) {
        return videoService.updateVideoStatusWithError(videoId, status, errorMessage);
    }

    @PutMapping("/{videoId}/duration")
    public ResponseEntity<String> updateVideoDuration(@PathVariable String videoId,
                                                     @RequestParam String duration) {
        return videoService.updateVideoDuration(videoId, duration);
    }
}

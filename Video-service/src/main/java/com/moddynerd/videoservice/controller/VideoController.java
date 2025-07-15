package com.moddynerd.videoservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moddynerd.videoservice.service.VideoService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/video")
public class VideoController {

    @Autowired
    VideoService videoService;

    @Autowired
    ObjectMapper objectMapper;

    // DTO class to hold video metadata
    @Data
    public static class VideoMetadata {
        // Getters and setters
        private String title;
        private String description;
        private Boolean isPublic;
        private String channelId;
        private String uploaderId;

    }

    //Endpoint to upload video file and save video details
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("data") String jsonData) {
        try {
            // Parse JSON data
            VideoMetadata metadata = objectMapper.readValue(jsonData, VideoMetadata.class);

            return videoService.saveVideoDetails(
                    file,
                    metadata.getTitle(),
                    metadata.getDescription(),
                    metadata.getIsPublic(),
                    metadata.getChannelId(),
                    metadata.getUploaderId()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to upload video and save details.");
        }
    }

    @GetMapping("/check/{videoId}")
    public boolean checkVideoExists(@PathVariable String videoId) {
        return videoService.checkVideoExists(videoId);
    }

}

package com.moddynerd.videoservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moddynerd.videoservice.model.VideoDetailsDTO;
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

    @GetMapping("/{userId}/videos")
    public ResponseEntity<?> getUserVideos(@PathVariable String userId) {
        return videoService.getUserVideos(userId);
    }

    @PutMapping("/{videoId}")
    public ResponseEntity<String> updateVideoDetails(
            @PathVariable String videoId,
            @RequestBody VideoDetailsDTO videoDetailsDTO,
            @RequestHeader("X-User-Id") String userId
    ){
        return videoService.updateVideoDetails(videoId, videoDetailsDTO, userId);
    }

    @PostMapping("/{videoId}/thumbnail")
    public ResponseEntity<String> uploadThumbnail(
            @PathVariable String videoId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") String userId
    ){
        return videoService.uploadThumbnail(videoId, file, userId);
    }

    @GetMapping("/{videoId}/thumbnail")z
    public ResponseEntity<byte[]> getThumbnail(@PathVariable String videoId) {
        return videoService.getThumbnail(videoId);
    }
}

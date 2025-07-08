package com.moddynerd.videoservice.controller;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/stream")
public class StreamingController {

    @Value("${video.upload.directory}")
    private String uploadDirectory;

    // Serve master playlist for adaptive streaming
    @GetMapping("/{videoId}/master.m3u8")
    public ResponseEntity<Resource> getMasterPlaylist(@PathVariable String videoId) {
        try {
            Path masterPlaylistPath = Paths.get(uploadDirectory, videoId, "master_playlist.m3u8");

            if (!Files.exists(masterPlaylistPath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(masterPlaylistPath);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .header("Access-Control-Allow-Origin", "*")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Serve quality-specific playlists
    @GetMapping("/{videoId}/{quality}/playlist.m3u8")
    public ResponseEntity<Resource> getQualityPlaylist(
            @PathVariable String videoId,
            @PathVariable String quality) {
        try {
            Path playlistPath = Paths.get(uploadDirectory, videoId, quality, "playlist.m3u8");

            if (!Files.exists(playlistPath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(playlistPath);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .header("Access-Control-Allow-Origin", "*")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Serve video segments
    @GetMapping("/{videoId}/{quality}/{segment}")
    public ResponseEntity<Resource> getVideoSegment(
            @PathVariable String videoId,
            @PathVariable String quality,
            @PathVariable String segment) {
        try {
            Path segmentPath = Paths.get(uploadDirectory, videoId, quality, segment);

            if (!Files.exists(segmentPath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(segmentPath);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "video/mp2t")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                    .header("Access-Control-Allow-Origin", "*")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get video info endpoint
    @GetMapping("/{videoId}/info")
    public ResponseEntity<VideoStreamInfo> getVideoInfo(@PathVariable String videoId) {
        try {
            Path videoDir = Paths.get(uploadDirectory, videoId);
            Path masterPlaylist = videoDir.resolve("master_playlist.m3u8");

            if (!Files.exists(masterPlaylist)) {
                return ResponseEntity.notFound().build();
            }

            VideoStreamInfo info = new VideoStreamInfo();
            info.setVideoId(videoId);
            info.setMasterPlaylistUrl("/stream/" + videoId + "/master.m3u8");
            info.setStreamingType("HLS");
            info.setStatus("ready");

            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // DTO for video stream information
    @Data
    public static class VideoStreamInfo {
        private String videoId;
        private String masterPlaylistUrl;
        private String streamingType;
        private String status;

    }
}
package com.moddynerd.videostreaming.controller;


import com.moddynerd.videostreaming.model.VideoStreamInfo;
import com.moddynerd.videostreaming.model.VideoSummaryDTO;
import com.moddynerd.videostreaming.service.StreamingService;
import jakarta.ws.rs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stream")
public class StreamingController {

    @Autowired
    private StreamingService streamingService;

    // Serve master playlist for adaptive streaming
    @GetMapping("/{videoId}/master.m3u8")
    public ResponseEntity<Resource> getMasterPlaylist(@PathVariable String videoId){
        try {
            return streamingService.getMasterPlaylist(videoId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Serve quality-specific playlists
    @GetMapping("/{videoId}/{quality}/playlist.m3u8")
    public ResponseEntity<Resource> getQualityPlaylist(@PathVariable String videoId, @PathVariable String quality) {
        return streamingService.getQualityPlaylist(videoId, quality);
    }

    // Serve video segments
    @GetMapping("/{videoId}/{quality}/{segment}")
    public ResponseEntity<Resource> getVideoSegment(@PathVariable String videoId, @PathVariable String quality, @PathVariable String segment) {
        return streamingService.getVideoSegment(videoId, quality, segment);
    }

    // Get video info endpoint
    @GetMapping("/{videoId}/stream-info")
    public ResponseEntity<VideoStreamInfo> getVideoStreamInfo(@PathVariable String videoId) {
        return streamingService.getVideoStreamInfo(videoId);
    }

    @GetMapping("/{videoId}/details")
    public ResponseEntity<VideoSummaryDTO> getVideoDetails(@PathVariable String videoId){
        return streamingService.getVideoDetails(videoId);
    }

}

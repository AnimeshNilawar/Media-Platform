package com.moddynerd.videoprocessingservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moddynerd.videoprocessingservice.model.VideoProcessingRequest;
import com.moddynerd.videoprocessingservice.service.VideoProcessingService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/video/process")
@Slf4j
public class VideoProcessingController {

    @Autowired
    private VideoProcessingService videoProcessingService;

    @PostMapping("/init")
    public ResponseEntity<String> processVideo(@RequestBody VideoProcessingRequest request) {
        log.info("Received video processing request for video ID: {}", request.getVideoId());

        try {
            videoProcessingService.processVideo(request);
            return new ResponseEntity<>("Video processing started successfully for video ID: " + request.getVideoId(),
                    HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters for video ID {}: {}", request.getVideoId(), e.getMessage());
            return new ResponseEntity<>("Invalid request parameters for video: " + request.getVideoId(),
                    HttpStatus.BAD_REQUEST);
        } catch (NullPointerException e) {
            log.error("Null value encountered for video ID {}: {}", request.getVideoId(), e.getMessage(), e);
            return new ResponseEntity<>("Invalid video processing request: missing required data",
                    HttpStatus.BAD_REQUEST);
        } catch (feign.FeignException.NotFound e) {
            log.error("Video not found for video ID {}: {}", request.getVideoId(), e.getMessage());
            return new ResponseEntity<>("Video not found: " + request.getVideoId(), HttpStatus.NOT_FOUND);
        } catch (feign.FeignException.BadRequest e) {
            log.error("Bad request for video ID {}: {}", request.getVideoId(), e.getMessage());
            return new ResponseEntity<>("Invalid request parameters for video: " + request.getVideoId(),
                    HttpStatus.BAD_REQUEST);
        } catch (feign.FeignException e) {
            log.error("External service error for video ID {}: {}", request.getVideoId(), e.getMessage());
            return new ResponseEntity<>("External service unavailable", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("Error starting video processing for video ID {}: {}", request.getVideoId(), e.getMessage(), e);
            return new ResponseEntity<>("Failed to start video processing: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

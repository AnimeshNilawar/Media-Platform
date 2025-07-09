package com.moddynerd.videoprocessingservice.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.moddynerd.videoprocessingservice.client.VideoServiceClient;
import com.moddynerd.videoprocessingservice.model.VideoProcessingRequest;
import com.moddynerd.videoprocessingservice.utils.MasterPlaylist;
import com.moddynerd.videoprocessingservice.utils.ProcessVideo;
import com.moddynerd.videoprocessingservice.utils.VideoDuration;
import com.moddynerd.videoprocessingservice.utils.VideoResolution;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VideoProcessingService {

    @Autowired
    private VideoServiceClient videoServiceClient;

    @Value("${video.processing.directory}")
    private String processingDirectory;

    @Value("${video.processing.thread-pool-size}")
    private int threadPoolSize;

    private final ExecutorService executorService;

    public VideoProcessingService(@Value("${video.processing.thread-pool-size}") int threadPoolSize) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    // Video Quality configs
    public static final class VideoQuality {
        public final String name;
        public final int height;
        public final String bitrate;

        public VideoQuality(String name, int height, String bitrate) {
            this.name = name;
            this.height = height;
            this.bitrate = bitrate;
        }
    }

    public static final VideoQuality[] QUALITY_LEVELS = {
            new VideoQuality("240p", 240, "400k"),
            new VideoQuality("360p", 360, "800k"),
            new VideoQuality("480p", 480, "1200k"),
            new VideoQuality("720p", 720, "2500k"),
            new VideoQuality("1080p", 1080, "5000k")
    };

    @Async
    public CompletableFuture<Object> processVideo(VideoProcessingRequest request) {
        log.info("Starting video processing for video ID: {}", request.getVideoId());

        try {
            // Validate required fields
            if (request.getVideoId() == null || request.getVideoId().trim().isEmpty()) {
                throw new IllegalArgumentException("Video ID cannot be null or empty");
            }
            if (request.getVideoPath() == null || request.getVideoPath().trim().isEmpty()) {
                throw new IllegalArgumentException("Video path cannot be null or empty");
            }

            videoServiceClient.updateVideoStatus(request.getVideoId(), "Processing");

            Path videoDir = Paths.get(processingDirectory, request.getVideoId());
            Path originalVideoPath = Paths.get(request.getVideoPath());

            if (Files.exists(videoDir)) {
                if (Files.isRegularFile(videoDir)) {
                    Files.delete(videoDir);
                    Files.createDirectories(videoDir);
                } else if (Files.isDirectory(videoDir)) {
                    log.info("Video directory already exists {}", videoDir);
                }
            } else {
                Files.createDirectories(videoDir);
            }

            Path originalInVideoDir = videoDir.resolve("original" + request.getFileExtension());

            if (Files.exists(originalInVideoDir)) {
                Files.delete(originalInVideoDir);
            }

            if (!Files.isSameFile(originalVideoPath.getParent(), videoDir)) {
                Files.move(originalVideoPath, originalInVideoDir);
            } else {
                Files.move(originalVideoPath, originalInVideoDir);
            }

            // Extract video duration and update in DB
            double duration = VideoDuration.getVideoDurationInSeconds(originalInVideoDir);
            videoServiceClient.updateVideoDuration(request.getVideoId(), String.valueOf(Math.round(duration)));

            // Get original video res
            VideoResolution originalResolution = VideoResolution.getVideoResolution(originalInVideoDir);
            log.info("Original video resolution: {}x{}", originalResolution.getWidth(), originalResolution.getHeight());

            List<CompletableFuture<Void>> processingTasks = new ArrayList<>();

            for (VideoQuality quality : QUALITY_LEVELS) {
                if (quality.height <= originalResolution.getHeight()) {
                    CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                        try {
                            log.info("Processinf video to quality: {}", quality.name);
                            ProcessVideo.processVideoToQuality(originalInVideoDir, videoDir, quality);
                        } catch (Exception e) {
                            log.error("Error processing video to quality {}: {}", quality.name, e.getMessage());
                        }
                    }, executorService);
                    processingTasks.add(task);
                }
            }

            // Wait for all processing tasks to complete
            CompletableFuture.allOf(processingTasks.toArray(new CompletableFuture[0])).join();

            // Generate master playlist after all qualities are processed
            MasterPlaylist.generateMasterPlaylist(videoDir, originalResolution);

            // Update video status to processed
            videoServiceClient.updateVideoStatus(request.getVideoId(), "Processed");
            log.info("Video processing completed for video ID: {}", request.getVideoId());

        } catch (Exception e) {
            log.error("Error processing video {}: {}", request.getVideoId(), e.getMessage(), e);
            videoServiceClient.updateVideoStatusWithError(request.getVideoId(), "Failed", e.getMessage());
        }

        return CompletableFuture.completedFuture(null);

    }

}

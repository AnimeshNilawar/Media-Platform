//package com.moddynerd.videoservice.service;
//
//import com.moddynerd.videoservice.dao.VideoDao;
//import com.moddynerd.videoservice.model.VideoDetails;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@Service
//public class VideoProcessingService {
//
//    @Autowired
//    private VideoDao videoDao;
//
//    @Value("${video.upload.directory}")
//    private String uploadDirectory;
//
//    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
//
//    // Video quality configurations
//    private static final class VideoQuality {
//        final String name;
//        final int height;
//        final String bitrate;
//
//        VideoQuality(String name, int height, String bitrate) {
//            this.name = name;
//            this.height = height;
//            this.bitrate = bitrate;
//        }
//    }
//
//    private static final VideoQuality[] QUALITY_LEVELS = {
//        new VideoQuality("240p", 240, "400k"),
//        new VideoQuality("360p", 360, "800k"),
//        new VideoQuality("480p", 480, "1200k"),
//        new VideoQuality("720p", 720, "2500k"),
//        new VideoQuality("1080p", 1080, "5000k")
//    };
//
//    @Async
//    public CompletableFuture<Void> processVideoAsync(String videoId, Path originalVideoPath) {
//        try {
//            // Create video directory structure
//            Path videoDir = Paths.get(uploadDirectory, videoId);
//
//            // Handle the case where there might be a file with the same name as the directory
//            if (Files.exists(videoDir)) {
//                if (Files.isRegularFile(videoDir)) {
//                    // If it's a file, delete it first so we can create the directory
//                    Files.delete(videoDir);
//                }
//            }
//
//            // Now create the directory
//            Files.createDirectories(videoDir);
//
//            // Move original video to the video directory
//            Path originalInVideoDir = videoDir.resolve("original" + getFileExtension(originalVideoPath));
//
//            // If destination exists, delete it first
//            if (Files.exists(originalInVideoDir)) {
//                Files.delete(originalInVideoDir);
//            }
//
//            // Move the original file to the video directory
//            Files.move(originalVideoPath, originalInVideoDir);
//
//            // Get original video resolution
//            VideoResolution originalResolution = getVideoResolution(originalInVideoDir);
//
//            // Process video in multiple formats
//            List<CompletableFuture<Void>> processingTasks = new ArrayList<>();
//
//            for (VideoQuality quality : QUALITY_LEVELS) {
//                // Only process if quality is less than or equal to original resolution
//                if (quality.height <= originalResolution.height) {
//                    CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
//                        try {
//                            processVideoToQuality(originalInVideoDir, videoDir, quality);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }, executorService);
//                    processingTasks.add(task);
//                }
//            }
//
//            // Wait for all processing tasks to complete
//            CompletableFuture.allOf(processingTasks.toArray(new CompletableFuture[0])).join();
//
//            // Generate master playlist after all qualities are processed
//            generateMasterPlaylist(videoDir, originalResolution);
//
//            // Update video status to "Processed"
//            updateVideoStatus(videoId, "Processed");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            updateVideoStatus(videoId, "Failed");
//        }
//
//        return CompletableFuture.completedFuture(null);
//    }
//
//    private void processVideoToQuality(Path originalVideo, Path videoDir, VideoQuality quality) throws IOException, InterruptedException {
//        // Create HLS segments instead of a single MP4 file
//        Path hlsDir = videoDir.resolve(quality.name);
//        Files.createDirectories(hlsDir);
//
//        Path playlistPath = hlsDir.resolve("playlist.m3u8");
//        String segmentPattern = hlsDir.resolve("segment_%03d.ts").toString();
//
//        ProcessBuilder pb = new ProcessBuilder(
//            "ffmpeg",
//            "-i", originalVideo.toString(),
//            "-vf", "scale=-2:" + quality.height,
//            "-c:v", "libx264",
//            "-b:v", quality.bitrate,
//            "-c:a", "aac",
//            "-b:a", "128k",
//            "-preset", "medium",
//            "-f", "hls",                    // HLS format
//            "-hls_time", "6",              // 6 second segments
//            "-hls_playlist_type", "vod",   // Video on Demand
//            "-hls_segment_filename", segmentPattern,
//            "-y", // Overwrite output file
//            playlistPath.toString()
//        );
//
//        Process process = pb.start();
//
//        // Log ffmpeg output for debugging
//        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//        String line;
//        while ((line = reader.readLine()) != null) {
//            System.out.println("FFmpeg: " + line); // Better logging
//        }
//
//        int exitCode = process.waitFor();
//        if (exitCode != 0) {
//            throw new RuntimeException("FFmpeg HLS processing failed for " + quality.name + " with exit code: " + exitCode);
//        }
//    }
//
//    private VideoResolution getVideoResolution(Path videoPath) throws IOException, InterruptedException {
//        ProcessBuilder pb = new ProcessBuilder(
//            "ffprobe",
//            "-v", "error",
//            "-select_streams", "v:0",
//            "-show_entries", "stream=width,height",
//            "-of", "csv=p=0",
//            videoPath.toString()
//        );
//
//        Process process = pb.start();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        String line = reader.readLine();
//        process.waitFor();
//
//        if (line != null && line.contains(",")) {
//            String[] dimensions = line.split(",");
//            int width = Integer.parseInt(dimensions[0].trim());
//            int height = Integer.parseInt(dimensions[1].trim());
//            return new VideoResolution(width, height);
//        }
//
//        // Default resolution if detection fails
//        return new VideoResolution(1920, 1080);
//    }
//
//    private String getFileExtension(Path path) {
//        String fileName = path.getFileName().toString();
//        int lastDotIndex = fileName.lastIndexOf('.');
//        return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : ".mp4";
//    }
//
//    private void updateVideoStatus(String videoId, String status) {
//        try {
//            VideoDetails videoDetails = videoDao.findById(videoId).orElse(null);
//            if (videoDetails != null) {
//                videoDetails.setUploadStatus(status);
//                videoDetails.setUpdatedAt(java.time.LocalDateTime.now().toString());
//                videoDao.save(videoDetails);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void generateMasterPlaylist(Path videoDir, VideoResolution originalResolution) throws IOException {
//        // Master playlist generation logic
//        Path masterPlaylistPath = videoDir.resolve("master_playlist.m3u8");
//        StringBuilder masterPlaylistContent = new StringBuilder();
//
//        // Add header
//        masterPlaylistContent.append("#EXTM3U\n");
//        masterPlaylistContent.append("#EXT-X-VERSION:3\n");
//
//        // Add stream information for each quality level
//        for (VideoQuality quality : QUALITY_LEVELS) {
//            // Only include qualities that were processed
//            if (quality.height <= originalResolution.height) {
//                masterPlaylistContent.append(String.format("#EXT-X-STREAM-INF:BANDWIDTH=%dk,RESOLUTION=%dx%d\n",
//                    Integer.parseInt(quality.bitrate.replace("k", "")), quality.height, quality.height));
//                masterPlaylistContent.append(quality.name + "/playlist.m3u8\n");
//            }
//        }
//
//        // Write to master playlist file
//        Files.write(masterPlaylistPath, masterPlaylistContent.toString().getBytes());
//    }
//
//    private static class VideoResolution {
//        final int width;
//        final int height;
//
//        VideoResolution(int width, int height) {
//            this.width = width;
//            this.height = height;
//        }
//    }
//}

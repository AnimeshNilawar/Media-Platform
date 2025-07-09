package com.moddynerd.videoservice.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.moddynerd.videoservice.Utils.GenerateVideoId;
import com.moddynerd.videoservice.dao.VideoDao;
import com.moddynerd.videoservice.dto.VideoProcessingRequest;
import com.moddynerd.videoservice.model.VideoDetails;

@Service
public class VideoService {

    @Autowired
    VideoDao videoDao;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${video.upload.directory}")
    private String uploadDirectory;

    @Value("${video.processing.service.url:http://localhost:8090}")
    private String videoProcessingServiceUrl;

    public ResponseEntity<String> saveVideoDetails(MultipartFile file, String title, String description,
            Boolean isPublic, String channelId, String uploaderId) {
        // Generate a unique VideoId
        String videoId = GenerateVideoId.generate();

        // Upload the file first to get the file path for duration extraction
        Path filePath;
        try {
            filePath = uploadVideoFile(file, videoId);
            if (filePath == null) {
                return ResponseEntity.status(500).body("Failed to upload video file.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to upload video file.");
        }

        // Extract video duration
        String duration = "0";
        try {
            double durationInSeconds = getVideoDurationInSeconds(filePath);
            duration = String.valueOf(Math.round(durationInSeconds));
        } catch (Exception e) {
            e.printStackTrace();
            // Continue with default duration if extraction fails
        }

        // Create a new VideoDetails object
        VideoDetails videoDetails = new VideoDetails();
        videoDetails.setId(videoId);
        videoDetails.setTitle(title);
        videoDetails.setDescription(description);
        videoDetails.setThumbnailUrl("Not Available"); // Default value until thumbnail is uploaded later
        videoDetails.setIsPublic(isPublic);
        videoDetails.setDuration(duration);
        videoDetails.setViewCount("0");
        videoDetails.setLikeCount("0");
        videoDetails.setDislikeCount("0");
        videoDetails.setCommentCount("0");
        videoDetails.setChannelId(channelId);
        videoDetails.setUploaderId(uploaderId);
        videoDetails.setUploadStatus("Processing");
        videoDetails.setPublishedAt(java.time.LocalDateTime.now().toString());
        videoDetails.setCreatedAt(java.time.LocalDateTime.now().toString());
        videoDetails.setUpdatedAt(java.time.LocalDateTime.now().toString());

        // Save the details to the database
        videoDao.save(videoDetails);

        // Send request to video processing service
        try {
            VideoProcessingRequest processingRequest = new VideoProcessingRequest();
            processingRequest.setVideoId(videoId);
            processingRequest.setVideoPath(filePath.toString());
            processingRequest.setOriginalFileName(file.getOriginalFilename());
            processingRequest.setFileExtension(getFileExtension(file.getOriginalFilename()));
            processingRequest.setFileSize(file.getSize());
            processingRequest.setUploaderId(uploaderId);
            processingRequest.setChannelId(channelId);

            restTemplate.postForObject(videoProcessingServiceUrl + "/video/process/init", processingRequest,
                    String.class);
        } catch (Exception e) {
            e.printStackTrace();
            // Continue even if processing service call fails - the video is already saved
            System.out
                    .println("Warning: Could not connect to video processing service. Video saved but not processed.");
        }

        return ResponseEntity.ok("Video uploaded successfully. Video ID: " + videoId);
    }

    private double getVideoDurationInSeconds(Path videoPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                videoPath.toString());
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();
        process.waitFor();
        if (line != null && !line.trim().isEmpty()) {
            return Double.parseDouble(line.trim());
        }
        return 0;
    }

    public Path uploadVideoFile(MultipartFile file, String VideoId) throws IOException {
        Path uploadDir = Paths.get(uploadDirectory);

        if (!Files.exists(uploadDir)) {
            try {
                Files.createDirectories(uploadDir);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        if (file.isEmpty()) {
            return null;
        }

        // Check if the file is a video file (basic check based on file extension)
        String fileName = file.getOriginalFilename();
        if (fileName == null || !(fileName.toLowerCase().endsWith(".mp4") ||
                fileName.toLowerCase().endsWith(".avi") ||
                fileName.toLowerCase().endsWith(".mov") ||
                fileName.toLowerCase().endsWith(".wmv") ||
                fileName.toLowerCase().endsWith(".flv") ||
                fileName.toLowerCase().endsWith(".mkv") ||
                fileName.toLowerCase().endsWith(".webm"))) {
            return null;
        }

        // Get the file extension from the original filename
        String fileExtension = getFileExtension(fileName);

        // Save with video ID + original extension
        Path filePath = uploadDir.resolve(VideoId + fileExtension);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    // Methods for video processing service communication
    public ResponseEntity<String> updateVideoStatus(String videoId, String status) {
        try {
            VideoDetails videoDetails = videoDao.findById(videoId).orElse(null);
            if (videoDetails == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video not found");
            }

            videoDetails.setUploadStatus(status);
            videoDetails.setUpdatedAt(java.time.LocalDateTime.now().toString());
            videoDao.save(videoDetails);

            return ResponseEntity.ok("Video status updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update video status");
        }
    }

    public ResponseEntity<String> updateVideoStatusWithError(String videoId, String status, String errorMessage) {
        try {
            VideoDetails videoDetails = videoDao.findById(videoId).orElse(null);
            if (videoDetails == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video not found");
            }

            videoDetails.setUploadStatus(status);
            videoDetails.setErrorDetails(errorMessage);
            videoDetails.setUpdatedAt(java.time.LocalDateTime.now().toString());
            videoDao.save(videoDetails);

            return ResponseEntity.ok("Video status and error updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update video status with error");
        }
    }

    public ResponseEntity<String> updateVideoDuration(String videoId, String duration) {
        try {
            VideoDetails videoDetails = videoDao.findById(videoId).orElse(null);
            if (videoDetails == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video not found");
            }

            videoDetails.setDuration(duration);
            videoDetails.setUpdatedAt(java.time.LocalDateTime.now().toString());
            videoDao.save(videoDetails);

            return ResponseEntity.ok("Video duration updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update video duration");
        }
    }

}

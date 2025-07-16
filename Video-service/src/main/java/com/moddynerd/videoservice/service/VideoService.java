package com.moddynerd.videoservice.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.moddynerd.videoservice.model.VideoDetailsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.moddynerd.videoservice.Utils.GenerateVideoId;
import com.moddynerd.videoservice.client.VideoProcessingServiceClient;
import com.moddynerd.videoservice.dao.VideoDao;
import com.moddynerd.videoservice.dto.VideoProcessingRequest;
import com.moddynerd.videoservice.model.VideoDetails;

@Service
public class VideoService {

    @Autowired
    VideoDao videoDao;

    @Autowired
    private VideoProcessingServiceClient videoProcessingServiceClient;

    @Value("${video.upload.directory}")
    private String uploadDirectory;

    @Value("${thumbnail.upload.directory}")
    private String thumbnailUploadDirectory;

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
        videoDetails.setUploaderId(uploaderId);
        videoDetails.setUploadStatus("Processing");
        videoDetails.setPublishedAt(java.time.LocalDateTime.now().toString());
        videoDetails.setCreatedAt(java.time.LocalDateTime.now().toString());
        videoDetails.setUpdatedAt(java.time.LocalDateTime.now().toString());

        // Save the details to the database
        videoDao.save(videoDetails);

        // Send request to video processing service using Feign client
        try {
            VideoProcessingRequest processingRequest = new VideoProcessingRequest();
            processingRequest.setVideoId(videoId);
            processingRequest.setVideoPath(filePath.toString());
            processingRequest.setOriginalFileName(file.getOriginalFilename());
            processingRequest.setFileExtension(getFileExtension(file.getOriginalFilename()));
            processingRequest.setFileSize(file.getSize());
            processingRequest.setUploaderId(uploaderId);
            processingRequest.setChannelId(channelId);

            videoProcessingServiceClient.processVideo(processingRequest);
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

    public boolean checkVideoExists(String videoId) {
        try {
            if (videoDao.existsById(videoId)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ResponseEntity<List<VideoDetailsDTO>> getUserVideos(String userId) {
        // Get all videos uploaded by a specific user and return as a list
        try {
            List<VideoDetails> videos = videoDao.findByUploaderId(userId);
            if (videos == null || videos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Convert VideoDetails to VideoDetailsDTO
            List<VideoDetailsDTO> videoDetailsDTOs = videos.stream()
                    .filter(video -> video.getErrorDetails() == null)
                    .map(video -> new VideoDetailsDTO(video.getId(), video.getTitle(), video.getDescription(),
                            video.getThumbnailUrl(), video.getDuration(), video.getIsPublic(),
                            video.getViewCount(), video.getUploadStatus(),
                            video.getPublishedAt()))
                    .toList();

            return ResponseEntity.ok(videoDetailsDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public ResponseEntity<String> updateVideoDetails(String videoId, VideoDetailsDTO videoDetailsDTO, String userId) {
        try {
            VideoDetails videoDetails = videoDao.findById(videoId).orElse(null);
            if (videoDetails == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video not found");
            }
            if (!Objects.equals(videoDetails.getUploaderId(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only update your own videos");
            }
            // Update video details
            videoDetails.setTitle(videoDetailsDTO.getTitle());
            videoDetails.setDescription(videoDetailsDTO.getDescription());
            videoDetails.setIsPublic(videoDetailsDTO.getIsPublic());
            videoDetails.setUpdatedAt(java.time.LocalDateTime.now().toString());
            videoDao.save(videoDetails);
            return ResponseEntity.ok("Video details updated successfully");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<String> uploadThumbnail(String videoId, MultipartFile file, String userId) {
        Optional<VideoDetails> videoOpt = videoDao.findById(videoId);
        if( videoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video not found");
        }
        VideoDetails video = videoOpt.get();
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !(fileName.toLowerCase().endsWith(".jpg") ||
                fileName.toLowerCase().endsWith(".jpeg") ||
                fileName.toLowerCase().endsWith(".png"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file type");
        }
        Path filePath;
        try {
            filePath = Paths.get(thumbnailUploadDirectory, videoId + "_" + URLEncoder.encode(fileName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Encoding error");
        }
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        video.setThumbnailUrl(filePath.toString());
        videoDao.save(video);
        return ResponseEntity.status(HttpStatus.OK).body("Thumbnail uploaded successfully");
    }

    public ResponseEntity<byte[]> getThumbnail(String videoId) {
        Optional<VideoDetails> videoOpt = videoDao.findById(videoId);
        if (videoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        VideoDetails video = videoOpt.get();
        String thumbnailUrl = video.getThumbnailUrl();
        if (thumbnailUrl == null || thumbnailUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Path thumbnailPath = Paths.get(thumbnailUrl);
        if (!Files.exists(thumbnailPath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        try {
            byte[] thumbnailBytes = Files.readAllBytes(thumbnailPath);
            return ResponseEntity.ok()
                    .header("Content-Type", getContentTypeForImage(thumbnailUrl))
                    .body(thumbnailBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private String getContentTypeForImage(String filePath) {
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }
}

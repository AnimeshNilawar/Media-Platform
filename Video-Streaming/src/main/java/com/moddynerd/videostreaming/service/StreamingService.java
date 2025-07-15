package com.moddynerd.videostreaming.service;

import com.moddynerd.videostreaming.dao.VideoDao;
import com.moddynerd.videostreaming.model.VideoDetails;
import com.moddynerd.videostreaming.model.VideoStreamInfo;
import com.moddynerd.videostreaming.model.VideoSummaryDTO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class StreamingService {

    private final VideoDao videoDao;
    @Value("${video.upload.directory}")
    private String uploadDirectory;

    public StreamingService(VideoDao videoDao) {
        this.videoDao = videoDao;
    }

    public ResponseEntity<Resource> getMasterPlaylist(String videoId) {
        try {
            Path masterPlaylistPath = Paths.get(uploadDirectory, videoId, "master_playlist.m3u8");

            if (!Files.exists(masterPlaylistPath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(masterPlaylistPath);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<Resource> getQualityPlaylist(String videoId, String quality) {
        try {
            Path playlistPath = Paths.get(uploadDirectory, videoId, quality, "playlist.m3u8");

            if (!Files.exists(playlistPath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(playlistPath);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<Resource> getVideoSegment(String videoId, String quality, String segment) {
        try {
            Path segmentPath = Paths.get(uploadDirectory, videoId, quality, segment);

            if (!Files.exists(segmentPath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(segmentPath);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "video/mp2t")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<VideoStreamInfo> getVideoStreamInfo(String videoId) {
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

    public ResponseEntity<VideoSummaryDTO> getVideoDetails(String videoId) {
        try {
            Optional<VideoDetails> videoOpt = videoDao.findById(videoId);

            if (videoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            VideoDetails video = videoOpt.get();


            return ResponseEntity.ok(new VideoSummaryDTO(
                    video.getId(),
                    video.getTitle(),
                    video.getDescription(),
                    video.getThumbnailUrl(),
                    video.getDuration(),
                    video.getIsPublic(),
                    video.getViewCount(),
                    video.getPublishedAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

package com.moddynerd.videoprocessingservice.utils;

import com.moddynerd.videoprocessingservice.service.VideoProcessingService;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class ProcessVideo {

    public static void processVideoToQuality(Path originalVideo, Path videoDir, VideoProcessingService.VideoQuality quality) throws IOException, InterruptedException {
        Path hlsDir = videoDir.resolve(quality.name);
        Files.createDirectories(hlsDir);

        Path playlistPath = hlsDir.resolve("playlist.m3u8");
        String segmentPattern = hlsDir.resolve("segment_%03d.ts").toString();

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", originalVideo.toString(),
                "-vf", "scale=-2:" + quality.height,
                "-c:v", "libx264",
                "-b:v", quality.bitrate,
                "-c:a", "aac",
                "-b:a", "128k",
                "-preset", "medium",
                "-f", "hls",
                "-hls_time", "6",
                "-hls_playlist_type", "vod",
                "-hls_segment_filename", segmentPattern,
                "-y",
                playlistPath.toString()
        );

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            log.debug("FFmpeg [{}]: {}", quality.name, line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg HLS processing failed for " + quality.name + " with exit code: " + exitCode);
        }
    }
}

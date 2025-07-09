package com.moddynerd.videoprocessingservice.utils;

import com.moddynerd.videoprocessingservice.service.VideoProcessingService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MasterPlaylist {

    private static final VideoProcessingService.VideoQuality[] QUALITY_LEVELS = VideoProcessingService.QUALITY_LEVELS;


    public static void generateMasterPlaylist(Path videoDir, VideoResolution originalResolution) throws IOException {
        Path masterPlaylistPath = videoDir.resolve("master_playlist.m3u8");
        StringBuilder masterPlaylistContent = new StringBuilder();

        masterPlaylistContent.append("#EXTM3U\n");
        masterPlaylistContent.append("#EXT-X-VERSION:3\n");

        for (VideoProcessingService.VideoQuality quality : QUALITY_LEVELS) {
            if (quality.height <= originalResolution.height) {
                masterPlaylistContent.append(String.format("#EXT-X-STREAM-INF:BANDWIDTH=%dk,RESOLUTION=%dx%d\n",
                        Integer.parseInt(quality.bitrate.replace("k", "")), quality.height, quality.height));
                masterPlaylistContent.append(quality.name + "/playlist.m3u8\n");
            }
        }

        Files.write(masterPlaylistPath, masterPlaylistContent.toString().getBytes());
    }
}

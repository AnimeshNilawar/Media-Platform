package com.moddynerd.videoprocessingservice.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class VideoResolution {
    final int width;
    final int height;

    public VideoResolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static VideoResolution getVideoResolution(Path videoPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=width,height",
                "-of", "csv=p=0",
                videoPath.toString()
        );

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();
        process.waitFor();

        if (line != null && line.contains(",")) {
            String[] dimensions = line.split(",");
            int width = Integer.parseInt(dimensions[0].trim());
            int height = Integer.parseInt(dimensions[1].trim());
            return new VideoResolution(width, height);
        }

        return new VideoResolution(1920, 1080);
    }

}
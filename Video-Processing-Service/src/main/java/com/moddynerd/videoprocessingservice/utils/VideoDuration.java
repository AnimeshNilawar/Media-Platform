package com.moddynerd.videoprocessingservice.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class VideoDuration {
    public static double getVideoDurationInSeconds(Path videoPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                videoPath.toString()
        );
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();
        process.waitFor();
        if (line != null && !line.trim().isEmpty()) {
            return Double.parseDouble(line.trim());
        }
        return 0;
    }
}


package com.moddynerd.videoservice.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoProcessingRequest implements Serializable {
    private String videoId;
    private String videoPath;
    private String originalFileName;
    private String fileExtension;
    private long fileSize;
    private String uploaderId;
}

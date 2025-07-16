package com.moddynerd.videoservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoDetailsDTO {
    private String id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String duration;
    private Boolean isPublic;
    private String viewCount;
    private String uploadStatus;
    private String publishedAt;
}

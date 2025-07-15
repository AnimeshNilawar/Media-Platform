package com.moddynerd.videostreaming.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoSummaryDTO {
    private String id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String duration;
    private Boolean isPublic;
    private String viewCount;
    private String publishedAt;
}

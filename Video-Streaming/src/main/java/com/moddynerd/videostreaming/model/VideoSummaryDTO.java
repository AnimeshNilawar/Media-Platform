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
    private String likeCount; // Number of likes
    private String dislikeCount; // Number of dislikes
    private String commentCount; // Number of comments
    private String publishedAt;
}

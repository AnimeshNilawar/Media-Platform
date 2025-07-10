package com.moddynerd.videostreaming.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class VideoDetails {

    @Id
    private String id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String duration; // Duration in ISO 8601 format (e.g., PT1H30M)
    private Boolean isPublic; // Visibility status (public/private/unlisted)
    private String viewCount; // Number of views
    private String likeCount; // Number of likes
    private String dislikeCount; // Number of dislikes
    private String commentCount; // Number of comments
    private String channelId;
    private String uploaderId; // ID of the user who uploaded the video
    private String uploadStatus; // Upload status (e.g., Processed, Failed)
    private String errorDetails; // Details of any errors during upload or processing
    private String publishedAt;
    private String createdAt; // Timestamp of when the video was added to the database
    private String updatedAt; // Timestamp of the last update to the video details
}

package com.moddynerd.videoservice.model;

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
    private String duration;
    private Boolean isPublic;
    private String viewCount;
    private String uploaderId;
    private String uploadStatus;
    private String errorDetails;
    private String publishedAt;
    private String createdAt;
    private String updatedAt;
}

package com.moddynerd.videostreaming.model;

import lombok.Data;

@Data
public class VideoStreamInfo {
    private String videoId;
    private String masterPlaylistUrl;
    private String streamingType;
    private String status;

}
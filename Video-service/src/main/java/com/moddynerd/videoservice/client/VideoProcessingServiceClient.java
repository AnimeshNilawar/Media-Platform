package com.moddynerd.videoservice.client;

import com.moddynerd.videoservice.dto.VideoProcessingRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "video-processing-service")
public interface VideoProcessingServiceClient {

    @PostMapping("/video/process/init")
    String processVideo(@RequestBody VideoProcessingRequest request);
}

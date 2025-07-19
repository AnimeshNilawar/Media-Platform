package com.moddynerd.engagementservice.controller;

import com.moddynerd.engagementservice.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; 

@RestController
@RequestMapping("/engage/like")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping("/{videoId}/{LikeValue}")
    public ResponseEntity<?> likeVideo(@PathVariable String videoId, @PathVariable Integer LikeValue, @RequestHeader("X-User-Id") String userId) {
        return likeService.likeVideo(videoId,LikeValue ,userId);
    }

    @GetMapping("/{videoId}/count")
    public ResponseEntity<?> getLikeCount(@PathVariable String videoId) {
        return likeService.getLikeCount(videoId);
    }

}

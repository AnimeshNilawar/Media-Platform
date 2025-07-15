package com.moddynerd.engagementservice.service;

import com.moddynerd.engagementservice.client.VideoClient;
import com.moddynerd.engagementservice.dao.LikeDao;
import com.moddynerd.engagementservice.model.like.Like;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private VideoClient videoClient;

    @Autowired
    private LikeDao likeDao;

    public boolean checkVideoExists(String videoId) {
        return videoClient.checkVideoExists(videoId);
    }

    public ResponseEntity<String> likeVideo(String videoId, Integer likeValue, String userId) {
        if (!checkVideoExists(videoId)) {
            return ResponseEntity.badRequest().body("Video does not exist");
        } else if (likeValue == null || (likeValue != 1 && likeValue != 0)) {
            return ResponseEntity.badRequest().body("Invalid like value. Use 1 for like and 0 for dislike.");
        } else if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body("User ID cannot be empty");
        } else {
            Like existingLike = likeDao.findByVideoIdAndUserId(videoId, userId);
            boolean isLiked = likeValue == 1;
            if (existingLike != null) {
                if (existingLike.isLiked() == isLiked) {
                    return ResponseEntity.badRequest().body("User has already " + (isLiked ? "liked" : "disliked") + " this video");
                } else {
                    existingLike.setLiked(isLiked);
                    likeDao.save(existingLike);
                    return ResponseEntity.ok("Video " + (isLiked ? "liked" : "disliked") + " successfully for video: " + videoId);
                }
            }
            Like like = new Like();
            like.setUserId(userId);
            like.setVideoId(videoId);
            like.setLiked(isLiked);
            likeDao.save(like);
            return ResponseEntity.ok("Video " + (isLiked ? "liked" : "disliked") + " successfully for video: " + videoId);
        }
    }
}

package com.moddynerd.engagementservice.service;

import com.moddynerd.engagementservice.client.UserClient;
import com.moddynerd.engagementservice.client.VideoClient;
import com.moddynerd.engagementservice.dao.CommentsDao;
import com.moddynerd.engagementservice.model.comment.Comment;
import com.moddynerd.engagementservice.model.comment.CommentDTO;
import com.moddynerd.engagementservice.model.comment.GetCommentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    @Autowired
    private VideoClient videoClient;

    @Autowired
    private CommentsDao commentsDao;

    @Autowired
    private UserClient userClient;

    public boolean checkVideoExists(String videoId) {
        return videoClient.checkVideoExists(videoId);
    }


    public ResponseEntity<String> addComment(String videoId, CommentDTO commentDTO) {
        if(!checkVideoExists(videoId)){
            return ResponseEntity.badRequest().body("Video does not exist");
        } else if (commentDTO.getUserId().isEmpty()) {
            return ResponseEntity.badRequest().body("User ID cannot be empty");
        } else if (commentDTO.getContent().isEmpty()) {
            return ResponseEntity.badRequest().body("Comment text cannot be empty");
        } else {
            Comment comment = new Comment();
            comment.setVideoId(videoId);
            comment.setUserId(commentDTO.getUserId());
            comment.setContent(commentDTO.getContent());

            commentsDao.save(comment);
            return ResponseEntity.ok("Comment added successfully for video: " + videoId);
        }

    }

    public ResponseEntity<List<GetCommentDTO>> getComments(String videoId) {
        if(!checkVideoExists(videoId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } else {
            List<Comment> comments = commentsDao.findByVideoId(videoId);

            // Get the usernames for each comment
            List<String> userIds = comments.stream()
                    .map(Comment::getUserId)
                    .distinct()
                    .toList();

            Map<String, String> usernames = userClient.getUsernames(userIds);

            List<GetCommentDTO> dtos = comments.stream()
                    .map(comment -> new GetCommentDTO(
                            comment.getId(),
                            comment.getVideoId(),
                            usernames.getOrDefault(comment.getUserId(), "Unknown User"),
                            comment.getContent(),
                            comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : null
                    ))
                    .toList();

            return ResponseEntity.ok(dtos);
        }
    }

    public ResponseEntity<String> updateComment(Integer commentId, CommentDTO commentDTO, String userId) {
        Comment comment = commentsDao.findById(commentId)
                .orElse(null);
        if (comment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
        }
        if (!comment.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only update your own comments");
        }
        String newContent = commentDTO.getContent();
        if (newContent == null || newContent.isEmpty()) {
            return ResponseEntity.badRequest().body("Comment text cannot be empty");
        }
        comment.setContent(newContent);
        comment.setUpdatedAt(LocalDateTime.now());
        commentsDao.save(comment);
        return ResponseEntity.ok("Comment updated successfully");
    }

    public ResponseEntity<?> deleteComment(Integer commentId, String userId) {
        Comment comment = commentsDao.findById(commentId)
                .orElse(null);
        if (comment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
        }
        if (!comment.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only delete your own comments");
        }
        commentsDao.deleteById(commentId);
        return ResponseEntity.ok("Comment deleted successfully");
    }
}

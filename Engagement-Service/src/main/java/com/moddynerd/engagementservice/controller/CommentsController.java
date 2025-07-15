package com.moddynerd.engagementservice.controller;

import com.moddynerd.engagementservice.model.comment.CommentDTO;
import com.moddynerd.engagementservice.model.comment.GetCommentDTO;
import com.moddynerd.engagementservice.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/engage/comment")
public class CommentsController {

    @Autowired
    private CommentService commentService;

    @PostMapping("add/{videoId}")
    public ResponseEntity<String> addComment(@PathVariable String videoId, @RequestBody CommentDTO commentDTO) {
        return commentService.addComment(videoId, commentDTO);
    }

    @GetMapping("/get/{videoId}")
    public ResponseEntity<List<GetCommentDTO>> getComments(@PathVariable String videoId) {
        return commentService.getComments(videoId);
    }

    @PutMapping("/update/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Integer commentId,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CommentDTO commentDTO
    ) {
        return commentService.updateComment(commentId, commentDTO, userId);
    }

    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<?> deleteComment (
            @PathVariable Integer commentId,
            @RequestHeader("X-User-Id") String userId){

        return commentService.deleteComment(commentId, userId);

    }

}

package com.moddynerd.engagementservice.dao;

import com.moddynerd.engagementservice.model.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsDao extends JpaRepository<Comment, Integer> {
    List<Comment> findByVideoId(String videoId);
}

package com.moddynerd.engagementservice.dao;


import com.moddynerd.engagementservice.model.like.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeDao extends JpaRepository<Like, Integer> {
    Like findByVideoIdAndUserId(String videoId, String userId);
    Integer countByVideoIdAndLiked(String videoId, boolean liked);
}

package com.moddynerd.videoservice.dao;

import com.moddynerd.videoservice.model.VideoDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoDao extends JpaRepository<VideoDetails, String> {
    List<VideoDetails> findByUploaderId(String uploaderId);
}

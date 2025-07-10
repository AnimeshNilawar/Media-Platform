package com.moddynerd.videostreaming.dao;

import com.moddynerd.videostreaming.model.VideoDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoDao extends JpaRepository<VideoDetails, String> {
}

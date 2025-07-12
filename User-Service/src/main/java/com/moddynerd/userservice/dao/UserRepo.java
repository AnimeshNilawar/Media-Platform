package com.moddynerd.userservice.dao;

import com.moddynerd.userservice.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<UserDetails, String> {
    Optional<UserDetails> findByUsername(String username);
}

package com.moddynerd.userservice.service;

import com.moddynerd.userservice.Utils.UserIdGenerator;
import com.moddynerd.userservice.model.UserDetails;
import com.moddynerd.userservice.dao.UserRepo;
import com.moddynerd.userservice.Utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private JwtUtil jwtUtil;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private String UID = UserIdGenerator.generate();

    public ResponseEntity<Map<String, Object>> register(UserDetails user) {
        Map<String, Object> response = new HashMap<>();
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            response.put("message", "Username already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setId(UID);
        userRepo.save(user);
        response.put("message", "User registered successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public ResponseEntity<Map<String, Object>> login(UserDetails loginRequest) {
        Map<String, Object> response = new HashMap<>();
        Optional<UserDetails> userOpt = userRepo.findByUsername(loginRequest.getUsername());
        if (userOpt.isPresent() && passwordEncoder.matches(loginRequest.getPassword(), userOpt.get().getPassword())) {
            response.put("token", jwtUtil.generateToken(userOpt.get().getId()));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        response.put("message", "Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    public Map<String, String> getUsernames(List<String> userIds) {
        Map<String, String> usernames = new HashMap<>();
        for (String userId : userIds) {
            Optional<UserDetails> userOpt = userRepo.findById(userId);
            if (userOpt.isPresent()) {
                usernames.put(userId, userOpt.get().getUsername());
            } else {
                usernames.put(userId, "Unknown User");
            }
        }
        return usernames;
    }
}

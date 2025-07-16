package com.moddynerd.userservice.service;

import com.moddynerd.userservice.Utils.UserIdGenerator;
import com.moddynerd.userservice.client.VideoClient;
import com.moddynerd.userservice.model.UserDetails;
import com.moddynerd.userservice.dao.UserRepo;
import com.moddynerd.userservice.Utils.JwtUtil;
import com.moddynerd.userservice.model.UserDetailsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Value("${profilePic.upload.directory}")
    private String profilePicUploadDirectory;

    @Autowired
    private VideoClient videoClient;

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

    public ResponseEntity<UserDetailsDTO> getUserDetails(String userId) {
        Optional<UserDetails> userDet = userRepo.findById(userId);
        if(userDet.isEmpty()) {
            UserDetailsDTO emptyDto = new UserDetailsDTO();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(emptyDto);
        }else {
            UserDetails user = userDet.get();
            UserDetailsDTO userDetailsDTO = new UserDetailsDTO();
            userDetailsDTO.setFirstName(user.getFirstName());
            userDetailsDTO.setLastName(user.getLastName());
            userDetailsDTO.setUsername(user.getUsername());
            userDetailsDTO.setEmail(user.getEmail());
            userDetailsDTO.setProfilePictureUrl(user.getProfilePictureUrl());
            userDetailsDTO.setBio(user.getBio());
            userDetailsDTO.setCreatedAt(user.getCreatedAt());
            return ResponseEntity.status(HttpStatus.OK).body(userDetailsDTO);
        }
    }

    public ResponseEntity<String> uploadProfilePic(MultipartFile file, String userId){
        Optional<UserDetails> userOpt = userRepo.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        UserDetails user = userOpt.get();
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !(fileName.toLowerCase().endsWith(".jpg") ||
                fileName.toLowerCase().endsWith(".jpeg") ||
                fileName.toLowerCase().endsWith(".png"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file type");
        }
        Path filePath;
        try {
            filePath = Paths.get(profilePicUploadDirectory, userId + "_" + URLEncoder.encode(fileName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Encoding error");
        }
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        user.setProfilePictureUrl(filePath.toString());
        userRepo.save(user);
        return ResponseEntity.status(HttpStatus.OK).body("Profile picture uploaded successfully");

    }

    public ResponseEntity<String> updateUserDetails(UserDetailsDTO userDetailsDTO, String userId) {
        Optional<UserDetails> userOpt = userRepo.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } else {
            UserDetails user = userOpt.get();
            user.setFirstName(userDetailsDTO.getFirstName());
            user.setLastName(userDetailsDTO.getLastName());
            user.setUsername(userDetailsDTO.getUsername());
            user.setBio(userDetailsDTO.getBio());
            user.setUpdatedAt(LocalDateTime.now());
            userRepo.save(user);
            return ResponseEntity.status(HttpStatus.OK).body("User details updated successfully");
        }
    }

    public ResponseEntity<?> getUserVideos(String userId) {
        return videoClient.getUserVideos(userId);
    }
}

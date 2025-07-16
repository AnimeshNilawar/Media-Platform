package com.moddynerd.userservice.controller;

import com.moddynerd.userservice.model.UserDetailsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.moddynerd.userservice.model.UserDetails;
import com.moddynerd.userservice.service.UserService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/batch")
    public Map<String, String> getUsernames(@RequestBody List<String> userIds) {
        return userService.getUsernames(userIds);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDetails user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDetails loginRequest) {
        return userService.login(loginRequest);
    }

    @GetMapping("/details")
    public ResponseEntity<UserDetailsDTO> getUserDetails( @RequestHeader("X-User-Id") String userId) {
        return userService.getUserDetails(userId);
    }

    @PostMapping("/profilepic")
    public ResponseEntity<String> uploadProfilePic(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") String userId
    ) {
        return userService.uploadProfilePic(file, userId);
    }

    @PutMapping("/details/update")
    public ResponseEntity<String> updateUserDetails(
            @RequestBody UserDetailsDTO userDetailsDTO,
            @RequestHeader("X-User-Id") String userId
    ){
        return userService.updateUserDetails(userDetailsDTO, userId);
    }

    @GetMapping("/videos")
    public ResponseEntity<?> getUserVideos(@RequestHeader("X-User-Id") String userId) {
        return userService.getUserVideos(userId);
    }



}

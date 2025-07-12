package com.moddynerd.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.moddynerd.userservice.model.UserDetails;
import com.moddynerd.userservice.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDetails user) {
        String result = userService.register(user);
        if (result == null) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDetails loginRequest) {
        String token = userService.login(loginRequest);
        if (token != null) {
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}

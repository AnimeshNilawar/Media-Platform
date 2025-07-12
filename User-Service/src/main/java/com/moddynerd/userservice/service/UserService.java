package com.moddynerd.userservice.service;

import com.moddynerd.userservice.Utils.UserIdGenerator;
import com.moddynerd.userservice.model.UserDetails;
import com.moddynerd.userservice.dao.UserRepo;
import com.moddynerd.userservice.Utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private JwtUtil jwtUtil;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private String UID = UserIdGenerator.generate();

    public String register(UserDetails user) {
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            return null;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setId(UID);
        userRepo.save(user);
        return "User registered successfully";
    }

    public String login(UserDetails loginRequest) {
        Optional<UserDetails> userOpt = userRepo.findByUsername(loginRequest.getUsername());
        if (userOpt.isPresent() && passwordEncoder.matches(loginRequest.getPassword(), userOpt.get().getPassword())) {
            return jwtUtil.generateToken(userOpt.get().getId());
        }
        return null;
    }
}


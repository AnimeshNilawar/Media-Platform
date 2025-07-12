package com.moddynerd.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
public class UserDetails {

    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String username;

    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    private String bio;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

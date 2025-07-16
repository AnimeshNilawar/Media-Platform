package com.moddynerd.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsDTO {

    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    private String bio;
    private LocalDateTime createdAt;
}

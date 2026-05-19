package com.utc2.appreborn.backend.modules.auth.entity;

import com.utc2.appreborn.backend.common.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password_hash")
    private String password;

    @Column(name = "auth_provider")
    private String authProvider;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean enabled;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
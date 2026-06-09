package com.utc2.appreborn.backend.modules.aichat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_action_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "action_type", length = 100, nullable = false)
    private String actionType;

    @Column(name = "action_label")
    private String actionLabel;

    @Column(name = "action_data", columnDefinition = "TEXT")
    private String actionData;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

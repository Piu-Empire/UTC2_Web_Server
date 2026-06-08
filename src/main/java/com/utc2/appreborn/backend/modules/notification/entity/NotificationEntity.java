package com.utc2.appreborn.backend.modules.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Maps to bảng NOTIFICATION trong DB.
 *
 * Quan hệ: notification.user_id → USER
 *
 * source:
 *   "SYSTEM" — thông báo do server tạo (cảnh báo học vụ, học phí, v.v.)
 *   "GMAIL"  — placeholder nếu muốn lưu email quan trọng từ Gmail về DB
 */
@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    /** FK → USER */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    /**
     * Phân loại thông báo:
     * ACADEMIC_WARNING, FEE_DUE, SCHEDULE_CHANGE,
     * ENROLLMENT_UPDATE, DORMITORY_STATUS, GENERAL
     */
    @Column(name = "type", length = 50)
    private String type;

    /**
     * Nguồn thông báo:
     *   "SYSTEM" — từ server (default)
     *   "GMAIL"  — proxy từ Gmail
     */
    @Column(name = "source", length = 20, nullable = false)
    @Builder.Default
    private String source = "SYSTEM";

    /** Entity liên quan (VD: "semester", "fee", "enrollment") */
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @CreationTimestamp
    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;

    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;
}

package com.utc2.appreborn.backend.modules.public_services.entity;

import com.utc2.appreborn.backend.modules.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_request")   // ← sửa từ "service_requests"
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String serviceType;     // TRANSCRIPT | CONFIRMATION_LETTER | CARD_REISSUE | LOAN_SUPPORT

    @Column(columnDefinition = "TEXT")
    private String description;     // lý do / mục đích / JSON chi tiết

    @Builder.Default
    private String status = "PENDING";  // PENDING | PROCESSING | COMPLETED | REJECTED

    private String resultNote;      // ghi chú kết quả từ nhà trường
    private String attachmentUrl;   // file đính kèm nếu có

    private LocalDateTime submittedAt;
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }
}
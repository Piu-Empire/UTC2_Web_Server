package com.utc2.appreborn.backend.modules.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "academic_warning")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicWarningEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warning_id")
    private Long warningId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    @Column(name = "warning_type")
    private String warningType;

    @Column(name = "description")
    private String description;

    @Column(name = "issued_at", insertable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "status")
    private String status;
}
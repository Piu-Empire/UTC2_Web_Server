package com.utc2.appreborn.backend.modules.assessment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "external_assessment_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ExternalAssessmentStatusId.class)
public class ExternalAssessmentStatus {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "period_id", nullable = false, length = 50)
    private String periodId;

    @Column(name = "advisor_approved", nullable = false)
    private boolean advisorApproved;

    @Column(name = "khoa_approved", nullable = false)
    private boolean khoaApproved;

    @Column(name = "truong_approved", nullable = false)
    private boolean truongApproved;

    @Column(name = "advisor_approved_at")
    private LocalDateTime advisorApprovedAt;

    @Column(name = "khoa_approved_at")
    private LocalDateTime khoaApprovedAt;

    @Column(name = "truong_approved_at")
    private LocalDateTime truongApprovedAt;
}
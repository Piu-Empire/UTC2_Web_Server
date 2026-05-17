package com.utc2.appreborn.backend.modules.assessment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_period")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentPeriod {

    @Id
    @Column(name = "period_id", length = 50)
    private String periodId;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
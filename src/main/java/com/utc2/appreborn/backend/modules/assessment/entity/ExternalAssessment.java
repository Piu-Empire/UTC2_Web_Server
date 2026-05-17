package com.utc2.appreborn.backend.modules.assessment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "external_assessment",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_external_assessment",
                columnNames = {"user_id", "period_id", "criteria_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "period_id", nullable = false, length = 50)
    private String periodId;

    @Column(name = "criteria_id", nullable = false)
    private Integer criteriaId;

    @Column(name = "tap_the_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal tapTheScore;

    @Column(name = "khoa_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal khoaScore;

    @Column(name = "truong_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal truongScore;

    @Column(name = "imported_at", insertable = false, updatable = false)
    private LocalDateTime importedAt;
}
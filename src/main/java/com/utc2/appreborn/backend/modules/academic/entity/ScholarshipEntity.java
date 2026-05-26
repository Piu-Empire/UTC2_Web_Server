package com.utc2.appreborn.backend.modules.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "scholarship")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scholarship_id")
    private Long scholarshipId;

    @Column(name = "name")
    private String name;

    @Column(name = "organization")
    private String organization;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "unit")
    private String unit;

    @Column(name = "min_gpa", precision = 3, scale = 2)
    private BigDecimal minGpa;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}

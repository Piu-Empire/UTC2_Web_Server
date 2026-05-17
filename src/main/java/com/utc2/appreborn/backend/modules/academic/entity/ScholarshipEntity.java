package com.utc2.appreborn.backend.modules.academic.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scholarship")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScholarshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scholarship_id")
    private Long scholarshipId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "organization")
    private String organization;

    @Column(name = "amount", columnDefinition = "DECIMAL(15,2)")
    private java.math.BigDecimal amount;

    @Column(name = "unit")
    private String unit;

    @Column(name = "min_gpa", columnDefinition = "DECIMAL(3,2)")
    private java.math.BigDecimal minGpa;

    @Column(name = "description")
    private String description;
}
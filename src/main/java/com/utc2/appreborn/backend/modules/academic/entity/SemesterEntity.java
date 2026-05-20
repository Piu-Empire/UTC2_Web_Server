package com.utc2.appreborn.backend.modules.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "semester")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemesterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "semester_id")
    private Long semesterId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "semester_name")
    private String semesterName;

    @Column(name = "academic_year")
    private String academicYear;

    @Column(name = "semester_number")
    private Integer semesterNumber;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "gpa", columnDefinition = "DECIMAL(4,2)")
    private java.math.BigDecimal gpa;

    @Column(name = "total_credits")
    private Integer totalCredits;

    @Column(name = "passed_credits")
    private Integer passedCredits;
}
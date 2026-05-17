package com.utc2.appreborn.backend.modules.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    @Column(name = "registered_at", insertable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "status")
    private String status;

    @Column(name = "midterm_score", columnDefinition = "DECIMAL(4,2)")
    private Double midtermScore;

    @Column(name = "final_score", columnDefinition = "DECIMAL(4,2)")
    private Double finalScore;

    @Column(name = "assignment_score", columnDefinition = "DECIMAL(4,2)")
    private Double assignmentScore;

    @Column(name = "total_score", columnDefinition = "DECIMAL(4,2)")
    private Double totalScore;

    @Column(name = "letter_grade")
    private String letterGrade;

    @Column(name = "grade_point", columnDefinition = "DECIMAL(3,2)")
    private Double gradePoint;

    @Column(name = "is_passed")
    private Boolean isPassed;
}
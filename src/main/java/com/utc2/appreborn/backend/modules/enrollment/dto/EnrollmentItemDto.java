package com.utc2.appreborn.backend.modules.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentItemDto {
    private Long   enrollmentId;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private String semesterName;
    private Integer semesterNumber;
    private String academicYear;
    private String status;
    private Double midtermScore;
    private Double finalScore;
    private Double assignmentScore;
    private Double totalScore;
    private String letterGrade;
    private Double gradePoint;
    private Boolean isPassed;
    private String registeredAt;
}
package com.utc2.appreborn.backend.modules.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradesByCourseDto {
    private Long   enrollmentId;
    private Long   userId;
    private String studentCode;
    private String fullName;
    private String className;
    private Integer credits;
    private Double midtermScore;
    private Double finalScore;
    private Double assignmentScore;
    private Double totalScore;
    private String letterGrade;
    private Double gradePoint;
    private Boolean isPassed;
    private String status;
}
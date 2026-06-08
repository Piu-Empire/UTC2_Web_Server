package com.utc2.appreborn.backend.modules.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseGradeDto {
    private Long    enrollmentId;
    private String  courseCode;
    private String  courseName;
    private Integer credits;
    private Double  midtermScore;
    private Double  finalScore;
    private Double  assignmentScore;
    private Double  totalScore;
    private String  letterGrade;
    private Double  gradePoint;
    private Boolean isPassed;
    private String  status;
    private Long    semesterId;
    private String  semesterName;
    private String  academicYear;
}

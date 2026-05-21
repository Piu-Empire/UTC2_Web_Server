package com.utc2.appreborn.backend.modules.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseItemDto {
    private Long    courseId;
    private String  courseCode;
    private String  courseName;
    private Integer credits;
    private Integer theoryHours;
    private Integer practiceHours;
    private String  department;
    private String  description;
    private Boolean alreadyEnrolled;
}

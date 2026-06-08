package com.utc2.appreborn.backend.modules.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherCourseDto {
    private Long   id;
    private Long   courseId;
    private String courseCode;
    private String courseName;
    private Long   semesterId;
    private String semesterName;
    private String className;
}
package com.utc2.appreborn.backend.modules.imports.dto;

import lombok.Data;

@Data
public class CourseImportRow {
    private String course_code;  // Phải khớp với header trong Excel
    private String course_name;
    private Integer credits;
    private String description;
}
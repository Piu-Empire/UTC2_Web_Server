package com.utc2.appreborn.backend.modules.imports.dto;

import lombok.Data;

/**
 * Cột Excel/CSV cho import chương trình đào tạo:
 * major | academic_year | course_code | semester_suggestion | is_required | group_name
 */
@Data
public class CurriculumImportRow {
    private String major;
    private String academicYear;
    private String courseCode;
    private String semesterSuggestion;
    private String isRequired;     // "true" / "false"
    private String groupName;
}
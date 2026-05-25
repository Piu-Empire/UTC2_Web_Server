package com.utc2.appreborn.backend.modules.imports.dto;

import lombok.Data;

/**
 * Cột Excel/CSV cho import profile sinh viên:
 * student_code | full_name | phone_number | date_of_birth | gender | address
 * | faculty | major | academic_year | class_name | status
 */
@Data
public class ProfileImportRow {
    private String studentCode;
    private String fullName;
    private String phoneNumber;
    private String dateOfBirth;   // yyyy-MM-dd
    private String gender;
    private String address;
    private String faculty;
    private String major;
    private String academicYear;
    private String className;
    private String status;
}
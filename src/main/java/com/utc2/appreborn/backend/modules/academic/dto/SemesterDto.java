package com.utc2.appreborn.backend.modules.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterDto {
    private Long    semesterId;
    private String  semesterName;
    private String  academicYear;
    private Integer semesterNumber;
    private String  startDate;
    private String  endDate;
    private Double  gpa;
    private Integer totalCredits;
    private Integer passedCredits;
    // Thông tin sinh viên (dùng cho Admin web)
    private String  fullName;
    private String  studentCode;
}

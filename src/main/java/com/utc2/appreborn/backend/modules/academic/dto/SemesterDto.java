package com.utc2.appreborn.backend.modules.academic.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SemesterDto {
    private Long   semesterId;
    private String semesterName;
    private String academicYear;
    private Integer semesterNumber;
    private String startDate;
    private String endDate;
    private java.math.BigDecimal gpa;
    private Integer totalCredits;
    private Integer passedCredits;
}
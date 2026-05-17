package com.utc2.appreborn.backend.modules.profile.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Long   id;
    private String studentId;    // MSSV
    private String email;
    private String fullName;
    private String phoneNumber;
    private String gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private String faculty;
    private String major;
    private String academicYear;
    private String className;
    private String status;
    private String avatarUrl;
    private String studentCardUrl;
}

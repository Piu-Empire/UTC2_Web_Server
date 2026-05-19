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
    private String studentId;       // MSSV
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String address;

    // FIX BUG 1+3: Đảm bảo Jackson serialize LocalDate thành "yyyy-MM-dd" (String)
    // thay vì array số [2003,8,15] khi JavaTimeModule chưa được config toàn cục.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private String gender;
    private String faculty;
    private String major;
    private String academicYear;
    private String className;
    private String status;
    private String avatarUrl;
    private String studentCardUrl;
    private String role;
}
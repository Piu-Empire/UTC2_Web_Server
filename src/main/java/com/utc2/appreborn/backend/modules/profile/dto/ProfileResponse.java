package com.utc2.appreborn.backend.modules.profile.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private String gender;
    private String faculty;
    private String major;
    private String academicYear;
    private String className;
    private String status;
    private String studentCardUrl;
    private String role;
    private String avatarUrl;

    private String advisorName;     // Tên cố vấn học tập

    // ─── DATA ĐỔ VÀO CÁC TAB CHI TIẾT TRÊN FRONTEND ───
    private Double gpa;                                     // GPA tích lũy tổng
    private Map<String, List<Map<String, Object>>> grades;  // Bảng điểm nhóm theo học kỳ
    private List<Map<String, Object>> schedules;            // Thời khóa biểu
    private List<Map<String, Object>> fees;                 // Tình hình học phí
    private List<Map<String, Object>> warnings;             // Cảnh báo học vụ
}
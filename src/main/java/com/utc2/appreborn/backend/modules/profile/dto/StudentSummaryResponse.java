package com.utc2.appreborn.backend.modules.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * StudentSummaryResponse — dùng cho danh sách sinh viên ở trang admin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSummaryResponse {
    private Long   id;
    private String studentCode;
    private String fullName;
    private String faculty;
    private String cohort;      // academicYear
    private Double gpa;         // chưa có trong DB, trả null
    private String status;
    private String email;
    
    // Thêm trường này để hiển thị tên cố vấn
    private String advisorName; 
}
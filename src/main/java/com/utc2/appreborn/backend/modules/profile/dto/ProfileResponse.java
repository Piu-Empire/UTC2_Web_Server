package com.utc2.appreborn.backend.modules.profile.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

/**
 * ProfileResponse
 * ──────────────────────────────────────────────────────────────
 * Trả về thông tin profile của sinh viên đang đăng nhập.
 * Field names khớp với ProfileResponse.java phía app Android.
 *
 * App dùng các field này cho:
 *   - HomeFragment: fullName (tên hiển thị thanh toolbar)
 *   - QrFragment:   fullName + studentId (nội dung mã QR)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    /** USER.user_id */
    private Long id;

    /** STUDENT_PROFILE.student_code — MSSV, encode vào QR */
    private String studentId;

    /** USER.email */
    private String email;

    /** USER_PROFILE.full_name — tên hiển thị trên Home + QR */
    private String fullName;

    /** USER_PROFILE.phone_number */
    private String phoneNumber;

    /** USER_PROFILE.gender */
    private String gender;

    /** USER_PROFILE.date_of_birth — format "yyyy-MM-dd" */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    /** STUDENT_PROFILE.faculty */
    private String faculty;

    /** STUDENT_PROFILE.major */
    private String major;

    /** STUDENT_PROFILE.academic_year */
    private String academicYear;

    /** STUDENT_PROFILE.class_name */
    private String className;

    /** STUDENT_PROFILE.status */
    private String status;

    /** USER_PROFILE.avatar_url */
    private String avatarUrl;
}
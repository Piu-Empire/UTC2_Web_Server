package com.utc2.appreborn.backend.modules.profile.service.impl;

import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.profile.dto.ProfileResponse;
import com.utc2.appreborn.backend.modules.profile.dto.StudentSummaryResponse;
import com.utc2.appreborn.backend.modules.profile.entity.StudentProfileEntity;
import com.utc2.appreborn.backend.modules.profile.entity.UserProfileEntity;
import com.utc2.appreborn.backend.modules.profile.dto.UpdateProfileRequest;
import com.utc2.appreborn.backend.modules.profile.repository.StudentProfileRepository;
import com.utc2.appreborn.backend.modules.profile.repository.UserProfileRepository;
import com.utc2.appreborn.backend.modules.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public ProfileResponse getMyProfile(String username) {
        String studentCode = extractStudentCode(username);
        StudentProfileEntity sp = studentProfileRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy profile cho: " + studentCode));
        UserProfileEntity up = userProfileRepository.findById(sp.getUserId()).orElse(null);
        return toResponse(sp, up);
    }

    @Override
    public ProfileResponse getProfileByStudentId(String studentId) {
        StudentProfileEntity sp = studentProfileRepository.findByStudentCode(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên: " + studentId));
        UserProfileEntity up = userProfileRepository.findById(sp.getUserId()).orElse(null);
        return toResponse(sp, up);
    }

    @Override
    @Transactional
    public ProfileResponse updateMyProfile(String username, UpdateProfileRequest request) {
        String studentCode = extractStudentCode(username);
        StudentProfileEntity sp = studentProfileRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Profile không tồn tại"));

        UserProfileEntity up = userProfileRepository.findById(sp.getUserId())
                .orElseGet(() -> UserProfileEntity.builder().userId(sp.getUserId()).build());

        if (request.getFullName() != null) up.setFullName(request.getFullName());
        if (request.getPhone() != null) up.setPhoneNumber(request.getPhone());
        if (request.getAddress() != null) up.setAddress(request.getAddress());
        if (request.getDateOfBirth() != null) up.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) up.setGender(request.getGender().name());
        if (request.getAvatarUrl() != null) up.setAvatarUrl(request.getAvatarUrl());

        userProfileRepository.save(up);
        return toResponse(sp, up);
    }

    @Override
    public Page<StudentSummaryResponse> listStudents(String search, String faculty, String cohort, String status, Pageable pageable) {
        return studentProfileRepository.findAll(pageable).map(sp -> {
            UserProfileEntity up = userProfileRepository.findById(sp.getUserId()).orElse(null);

            // FIX: Lấy GPA mới nhất từ bảng semester (học kỳ gần nhất)
            Double gpa = null;
            try {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT gpa FROM semester WHERE user_id = ? ORDER BY semester_id DESC LIMIT 1",
                    sp.getUserId()
                );
                if (!rows.isEmpty() && rows.get(0).get("gpa") != null) {
                    gpa = ((Number) rows.get(0).get("gpa")).doubleValue();
                }
            } catch (Exception ignored) {}

            return StudentSummaryResponse.builder()
                    .id(sp.getUserId())
                    .studentCode(sp.getStudentCode())
                    .fullName(up != null ? up.getFullName() : null)
                    .faculty(sp.getFaculty())
                    .cohort(sp.getAcademicYear())
                    .gpa(gpa)
                    .status(sp.getStatus())
                    .email(sp.getUser() != null ? sp.getUser().getEmail() : null)
                    .advisorName(sp.getAdvisor() != null ? sp.getAdvisor().getFullName() : "Chưa phân công")
                    .build();
        });
    }

    private String extractStudentCode(String username) {
        return username.contains("@") ? username.split("@")[0] : username;
    }

    private ProfileResponse toResponse(StudentProfileEntity sp, UserProfileEntity up) {
        Long userId = sp.getUserId();

        List<Map<String, Object>> gradesList = new ArrayList<>();
        List<Map<String, Object>> fees = new ArrayList<>();
        List<Map<String, Object>> schedules = new ArrayList<>();
        List<Map<String, Object>> warnings = new ArrayList<>();
        Double gpa = null;

        try {
            // FIX: JOIN enrollment với course để có courseCode và courseName
            gradesList = jdbcTemplate.queryForList(
                "SELECT e.*, c.course_code AS courseCode, c.course_name AS courseName, c.credits " +
                "FROM enrollment e " +
                "LEFT JOIN course c ON e.course_id = c.course_id " +
                "WHERE e.user_id = ?",
                userId
            );

            // FIX: JOIN fee với semester để có semester_name hiển thị
            fees = jdbcTemplate.queryForList(
                "SELECT f.*, s.semester_name AS semesterName " +
                "FROM fee f " +
                "LEFT JOIN semester s ON f.semester_id = s.semester_id " +
                "WHERE f.user_id = ?",
                userId
            );

            // FIX: JOIN schedule với course để có courseCode và courseName
            schedules = jdbcTemplate.queryForList(
                "SELECT sc.*, c.course_code AS courseCode, c.course_name AS courseName " +
                "FROM schedule sc " +
                "LEFT JOIN course c ON sc.course_id = c.course_id " +
                "WHERE sc.user_id = ?",
                userId
            );

            // FIX: Lấy cảnh báo học vụ — trước đây bị bỏ quên hoàn toàn
            warnings = jdbcTemplate.queryForList(
                "SELECT aw.*, s.semester_name AS semesterName " +
                "FROM academic_warning aw " +
                "LEFT JOIN semester s ON aw.semester_id = s.semester_id " +
                "WHERE aw.user_id = ?",
                userId
            );

            // FIX: Tính GPA tích lũy từ học kỳ gần nhất
            List<Map<String, Object>> gpaRows = jdbcTemplate.queryForList(
                "SELECT gpa FROM semester WHERE user_id = ? ORDER BY semester_id DESC LIMIT 1",
                userId
            );
            if (!gpaRows.isEmpty() && gpaRows.get(0).get("gpa") != null) {
                gpa = ((Number) gpaRows.get(0).get("gpa")).doubleValue();
            }

        } catch (Exception e) {
            System.err.println("Lỗi truy vấn dữ liệu phụ cho userId=" + userId + ": " + e.getMessage());
        }

        // Nhóm grades theo semester_id để frontend render đúng tab
        Map<String, List<Map<String, Object>>> gradesMap = new LinkedHashMap<>();
        for (Map<String, Object> row : gradesList) {
            Object semId = row.get("semester_id");
            String key = semId != null ? semId.toString() : "allGrades";
            gradesMap.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        return ProfileResponse.builder()
                .id(userId)
                .studentId(sp.getStudentCode())
                .fullName(up != null ? up.getFullName() : null)
                .email(sp.getUser() != null ? sp.getUser().getEmail() : null)
                .phoneNumber(up != null ? up.getPhoneNumber() : null)
                .address(up != null ? up.getAddress() : null)
                .dateOfBirth(up != null ? up.getDateOfBirth() : null)
                .gender(up != null ? up.getGender() : null)
                .avatarUrl(up != null ? up.getAvatarUrl() : null)
                .faculty(sp.getFaculty())
                .major(sp.getMajor())
                .academicYear(sp.getAcademicYear())
                .className(sp.getClassName())
                .status(sp.getStatus())
                .studentCardUrl(sp.getStudentCardUrl())
                .advisorName(sp.getAdvisor() != null ? sp.getAdvisor().getFullName() : "Chưa phân công")
                .gpa(gpa)
                .grades(gradesMap)
                .fees(fees)
                .schedules(schedules)
                .warnings(warnings)
                .build();
    }
}
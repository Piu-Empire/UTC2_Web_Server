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
            return StudentSummaryResponse.builder()
                    .id(sp.getUserId())
                    .studentCode(sp.getStudentCode())
                    .fullName(up != null ? up.getFullName() : null)
                    .faculty(sp.getFaculty())
                    .cohort(sp.getAcademicYear())
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

    try {
        // 1. Dùng bảng 'enrollment' (không có s)
        gradesList = jdbcTemplate.queryForList("SELECT * FROM enrollment WHERE user_id = ?", userId);
        
        // 2. Dùng bảng 'fee' (không có s) và query bằng user_id
        fees = jdbcTemplate.queryForList("SELECT * FROM fee WHERE user_id = ?", userId);
        
        // 3. Dùng bảng 'schedule' (không có s)
        schedules = jdbcTemplate.queryForList("SELECT * FROM schedule WHERE user_id = ?", userId);
    } catch (Exception e) {
        System.err.println("Lỗi truy vấn dữ liệu phụ: " + e.getMessage());
    }

    Map<String, List<Map<String, Object>>> gradesMap = new HashMap<>();
    gradesMap.put("allGrades", gradesList); 

    return ProfileResponse.builder()
            .id(userId)
            .studentId(sp.getStudentCode())
            .fullName(up != null ? up.getFullName() : null)
            .email(sp.getUser() != null ? sp.getUser().getEmail() : null)
            .faculty(sp.getFaculty())
            .major(sp.getMajor())
            .academicYear(sp.getAcademicYear())
            .className(sp.getClassName())
            .status(sp.getStatus())
            .grades(gradesMap)
            .fees(fees)
            .schedules(schedules)
            .build();
}
}
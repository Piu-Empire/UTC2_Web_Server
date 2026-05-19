package com.utc2.appreborn.backend.modules.profile.service.impl;

import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.profile.dto.ProfileResponse;
import com.utc2.appreborn.backend.modules.profile.dto.UpdateProfileRequest;
import com.utc2.appreborn.backend.modules.profile.entity.StudentProfile;
import com.utc2.appreborn.backend.modules.profile.entity.UserProfile;
import com.utc2.appreborn.backend.modules.profile.repository.StudentProfileRepository;
import com.utc2.appreborn.backend.modules.profile.repository.UserProfileRepository;
import com.utc2.appreborn.backend.modules.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserProfileRepository    userProfileRepository;

    @Override
    public ProfileResponse getMyProfile(String username) {
        // FIX WARN 1: JWT subject là email (vd: 2211020001@st.utc2.edu.vn)
        // → extract MSSV trước khi tìm StudentProfile
        String studentCode = extractStudentCode(username);

        StudentProfile sp = studentProfileRepository.findByStudentCodeWithUser(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sinh viên với mã định danh: " + studentCode));

        // FIX: dùng findById thay vì findByUserId (userId là @Id)
        UserProfile up = userProfileRepository.findById(sp.getUser().getId()).orElse(null);

        return toResponse(sp, up);
    }

    @Override
    public ProfileResponse getProfileByStudentId(String studentId) {
        StudentProfile sp = studentProfileRepository.findByStudentCodeWithUser(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sinh viên với mã số: " + studentId));

        // FIX: findById
        UserProfile up = userProfileRepository.findById(sp.getUser().getId()).orElse(null);

        return toResponse(sp, up);
    }

    @Override
    @Transactional
    public ProfileResponse updateMyProfile(String username, UpdateProfileRequest request) {
        String studentCode = extractStudentCode(username);

        StudentProfile sp = studentProfileRepository.findByStudentCodeWithUser(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thông tin sinh viên cần cập nhật: " + studentCode));

        // FIX: findById thay vì findByUserId
        UserProfile up = userProfileRepository.findById(sp.getUser().getId())
                .orElse(UserProfile.builder()
                        .userId(sp.getUser().getId())
                        .user(sp.getUser())
                        .build());

        if (request.getFullName()    != null) up.setFullName(request.getFullName());
        if (request.getPhone()       != null) up.setPhoneNumber(request.getPhone());
        if (request.getDateOfBirth() != null) up.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender()      != null) up.setGender(request.getGender().toString());
        if (request.getAvatarUrl()   != null) up.setAvatarUrl(request.getAvatarUrl());
        if (request.getAddress()     != null) up.setAddress(request.getAddress());

        // save() có sẵn từ JpaRepository
        userProfileRepository.save(up);
        return toResponse(sp, up);
    }

    // ── Helpers ───────────────────────────────────────────────

    /**
     * FIX WARN 1: JWT subject = email (2211020001@st.utc2.edu.vn)
     * Nếu username chứa "@" → lấy phần trước "@" = studentCode
     * Nếu không có "@" → dùng thẳng
     */
    private String extractStudentCode(String username) {
        return username.contains("@") ? username.split("@")[0] : username;
    }

    private ProfileResponse toResponse(StudentProfile sp, UserProfile up) {
        return ProfileResponse.builder()
                .id(sp.getUserId())
                .studentId(sp.getStudentCode())
                .username(sp.getStudentCode())
                .email(sp.getUser().getEmail())
                .faculty(sp.getFaculty())
                .major(sp.getMajor())
                .academicYear(sp.getAcademicYear())
                .className(sp.getClassName())
                .status(sp.getStatus())
                .studentCardUrl(sp.getStudentCardUrl())
                .role(sp.getUser().getRole().name())
                .fullName(up != null ? up.getFullName()    : null)
                .phoneNumber(up != null ? up.getPhoneNumber() : null)
                .address(up != null ? up.getAddress()      : null)
                .dateOfBirth(up != null ? up.getDateOfBirth() : null)
                .gender(up != null ? up.getGender()        : null)
                .avatarUrl(up != null ? up.getAvatarUrl()  : null)
                .build();
    }
}
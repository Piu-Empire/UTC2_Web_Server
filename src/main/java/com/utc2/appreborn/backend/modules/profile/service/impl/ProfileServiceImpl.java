package com.utc2.appreborn.backend.modules.profile.service.impl;

import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.auth.entity.User;
import com.utc2.appreborn.backend.modules.auth.repository.UserRepository;
import com.utc2.appreborn.backend.modules.profile.dto.ProfileResponse;
import com.utc2.appreborn.backend.modules.profile.entity.StudentProfileEntity;
import com.utc2.appreborn.backend.modules.profile.entity.UserProfileEntity;
import com.utc2.appreborn.backend.modules.profile.repository.StudentProfileRepository;
import com.utc2.appreborn.backend.modules.profile.repository.UserProfileRepository;
import com.utc2.appreborn.backend.modules.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository            userRepository;
    private final UserProfileRepository     userProfileRepository;
    private final StudentProfileRepository  studentProfileRepository;

    @Override
    public ProfileResponse getMyProfile(String email) {
        // 1. Tìm User theo email (subject của JWT)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        Long userId = user.getId();

        // 2. Lấy UserProfile (tên, SĐT, ngày sinh, giới tính, avatar)
        Optional<UserProfileEntity> userProfileOpt = userProfileRepository.findByUserId(userId);

        // 3. Lấy StudentProfile (MSSV, khoa, ngành, lớp, trạng thái)
        Optional<StudentProfileEntity> studentProfileOpt = studentProfileRepository.findByUserId(userId);

        // 4. Build response — dùng null-safe fallback nếu chưa có dữ liệu
        ProfileResponse.ProfileResponseBuilder builder = ProfileResponse.builder()
                .id(userId)
                .email(user.getEmail());

        userProfileOpt.ifPresent(up -> builder
                .fullName(up.getFullName())
                .phoneNumber(up.getPhoneNumber())
                .gender(up.getGender())
                .dateOfBirth(up.getDateOfBirth())
                .avatarUrl(up.getAvatarUrl()));

        studentProfileOpt.ifPresent(sp -> builder
                .studentId(sp.getStudentCode())
                .faculty(sp.getFaculty())
                .major(sp.getMajor())
                .academicYear(sp.getAcademicYear())
                .className(sp.getClassName())
                .status(sp.getStatus()));

        return builder.build();
    }
}
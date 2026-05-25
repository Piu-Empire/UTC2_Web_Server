package com.utc2.appreborn.backend.modules.profile.service;

import com.utc2.appreborn.backend.modules.profile.dto.ProfileResponse;
import com.utc2.appreborn.backend.modules.profile.dto.StudentSummaryResponse;
import com.utc2.appreborn.backend.modules.profile.dto.UpdateProfileRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProfileService {
    ProfileResponse getMyProfile(String username);
    ProfileResponse getProfileByStudentId(String studentId);
    ProfileResponse updateMyProfile(String username, UpdateProfileRequest request);
    
    Page<StudentSummaryResponse> listStudents(
            String search,
            String faculty,
            String cohort,
            String status,
            Pageable pageable);
}
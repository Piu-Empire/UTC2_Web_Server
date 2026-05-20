package com.utc2.appreborn.backend.modules.profile.service;

import com.utc2.appreborn.backend.modules.profile.dto.ProfileResponse;
import com.utc2.appreborn.backend.modules.profile.dto.UpdateProfileRequest;

public interface ProfileService {
    ProfileResponse getMyProfile(String username);
    ProfileResponse getProfileByStudentId(String studentId);
    ProfileResponse updateMyProfile(String username, UpdateProfileRequest request);
}
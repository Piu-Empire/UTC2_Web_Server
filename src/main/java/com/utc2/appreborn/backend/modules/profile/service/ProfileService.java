package com.utc2.appreborn.backend.modules.profile.service;

import com.utc2.appreborn.backend.modules.profile.dto.ProfileResponse;

public interface ProfileService {
    ProfileResponse getMyProfile(String email);
}

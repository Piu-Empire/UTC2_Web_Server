package com.utc2.appreborn.backend.modules.profile.service;

import com.utc2.appreborn.backend.modules.profile.dto.ProfileResponse;

public interface ProfileService {
    /** Trả về profile của user đang đăng nhập (lấy email từ JWT). */
    ProfileResponse getMyProfile(String email);
}
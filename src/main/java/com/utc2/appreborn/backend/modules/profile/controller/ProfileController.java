package com.utc2.appreborn.backend.modules.profile.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.profile.dto.ProfileResponse;
import com.utc2.appreborn.backend.modules.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * GET /api/v1/profile/me
     *
     * Trả về thông tin profile của sinh viên đang đăng nhập.
     * Yêu cầu: Bearer JWT token hợp lệ trong header Authorization.
     *
     * App dùng endpoint này cho:
     *   - HomeFragment: hiển thị tên sinh viên trên toolbar
     *   - QrFragment:   hiển thị tên + mã QR từ MSSV
     *
     * Response: ApiResponse<ProfileResponse>
     *   { success: true, message: "Success", data: { fullName, studentId, ... } }
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        ProfileResponse profile = profileService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
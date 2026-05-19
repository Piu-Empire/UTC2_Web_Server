package com.utc2.appreborn.backend.modules.profile.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.profile.dto.ProfileResponse;
import com.utc2.appreborn.backend.modules.profile.dto.UpdateProfileRequest;
import com.utc2.appreborn.backend.modules.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "API thông tin sinh viên")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    /**
     * GET /api/v1/profile/me
     * Cần Bearer token — trả về thông tin profile của user đang đăng nhập
     */
    @GetMapping("/me")
    @Operation(summary = "Xem profile của chính mình")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                profileService.getMyProfile(userDetails.getUsername())));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Tra cứu sinh viên theo mã số")
    public ResponseEntity<ApiResponse<ProfileResponse>> getByStudentId(
            @PathVariable String studentId) {
        return ResponseEntity.ok(ApiResponse.success(
                profileService.getProfileByStudentId(studentId)));
    }

    @PutMapping("/me")
    @Operation(summary = "Cập nhật profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                profileService.updateMyProfile(userDetails.getUsername(), request)));
    }
}
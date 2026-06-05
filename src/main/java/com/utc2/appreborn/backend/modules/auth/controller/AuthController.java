package com.utc2.appreborn.backend.modules.auth.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.auth.dto.AuthResponse;
import com.utc2.appreborn.backend.modules.auth.dto.ForgotPasswordRequest;
import com.utc2.appreborn.backend.modules.auth.dto.GoogleLoginRequest;
import com.utc2.appreborn.backend.modules.auth.dto.LoginRequest;
import com.utc2.appreborn.backend.modules.auth.dto.RegisterRequest;
import com.utc2.appreborn.backend.modules.auth.dto.ResetPasswordRequest;
import com.utc2.appreborn.backend.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/login
     * Body: { "studentCode": "2211020001", "password": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    /**
     * POST /api/v1/auth/google
     * Body: { "idToken": "..." }
     * Chỉ chấp nhận email @st.utc2.edu.vn
     */
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.googleLogin(request)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(request)));
    }

    /**
     * POST /api/v1/auth/forgot-password
     * Body: { "email": "2211020001" } hoặc { "email": "2211020001@st.utc2.edu.vn" }
     * Gửi mã OTP về email sinh viên
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi mã OTP về email của bạn. Vui lòng kiểm tra hộp thư."));
    }

    /**
     * POST /api/v1/auth/reset-password
     * Body: { "email": "2211020001", "otp": "123456", "newPassword": "..." }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại."));
    }
}
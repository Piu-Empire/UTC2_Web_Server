package com.utc2.appreborn.backend.modules.auth.service;

import com.utc2.appreborn.backend.modules.auth.dto.AuthResponse;
import com.utc2.appreborn.backend.modules.auth.dto.ForgotPasswordRequest;
import com.utc2.appreborn.backend.modules.auth.dto.GoogleLoginRequest;
import com.utc2.appreborn.backend.modules.auth.dto.LoginRequest;
import com.utc2.appreborn.backend.modules.auth.dto.RegisterRequest;
import com.utc2.appreborn.backend.modules.auth.dto.ResetPasswordRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    AuthResponse googleLogin(GoogleLoginRequest request);

    AuthResponse register(RegisterRequest request);

    /** Gửi OTP đặt lại mật khẩu về email của user */
    void forgotPassword(ForgotPasswordRequest request);

    /** Xác minh OTP và đặt mật khẩu mới */
    void resetPassword(ResetPasswordRequest request);
}
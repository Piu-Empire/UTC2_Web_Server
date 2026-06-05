package com.utc2.appreborn.backend.modules.auth.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;   // email hoặc MSSV của user
    private String otp;     // Mã OTP nhận được qua email
    private String newPassword;
}

package com.utc2.appreborn.backend.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String studentCode; // App gửi MSSV thuần (vd: 2211020001)

    @NotBlank
    private String password;
}
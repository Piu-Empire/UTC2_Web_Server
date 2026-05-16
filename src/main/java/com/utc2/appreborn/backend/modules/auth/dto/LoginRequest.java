package com.utc2.appreborn.backend.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String email; // Đổi từ username thành email

    @NotBlank
    private String password;
}
package com.utc2.appreborn.backend.modules.auth.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String email; // Hoặc MSSV – sẽ ghép @st.utc2.edu.vn nếu cần
}

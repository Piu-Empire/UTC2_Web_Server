package com.utc2.appreborn.backend.modules.auth.service;

import com.utc2.appreborn.backend.modules.auth.dto.AuthResponse;
import com.utc2.appreborn.backend.modules.auth.dto.GoogleLoginRequest;
import com.utc2.appreborn.backend.modules.auth.dto.LoginRequest;
import com.utc2.appreborn.backend.modules.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    AuthResponse googleLogin(GoogleLoginRequest request);

    AuthResponse register(RegisterRequest request);
}
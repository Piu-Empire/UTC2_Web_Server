package com.utc2.appreborn.backend.modules.auth.service.impl;

import com.utc2.appreborn.backend.modules.auth.service.AuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.auth.dto.AuthResponse;
import com.utc2.appreborn.backend.modules.auth.dto.GoogleLoginRequest;
import com.utc2.appreborn.backend.modules.auth.dto.LoginRequest;
import com.utc2.appreborn.backend.modules.auth.dto.RegisterRequest;
import com.utc2.appreborn.backend.modules.auth.entity.User;
import com.utc2.appreborn.backend.modules.auth.repository.UserRepository;
import com.utc2.appreborn.backend.security.jwt.JwtService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${google.client.id:}")
    private String googleClientId;

    private static final String ALLOWED_DOMAIN = "st.utc2.edu.vn";

    private static final String STUDENT_DOMAIN = "@st.utc2.edu.vn";

    @Override
    public AuthResponse login(LoginRequest request) {
        // Nếu input đã là email đầy đủ (chứa @) thì dùng nguyên,
        // ngược lại ghép domain sinh viên: "2211020001" → "2211020001@st.utc2.edu.vn"
        String email = request.getStudentCode().contains("@")
                ? request.getStudentCode()
                : request.getStudentCode() + STUDENT_DOMAIN;

        // 1. Xác thực email + password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));

        // 2. Tạo JWT token
        var userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(userDetails);

        // 3. Lấy user + studentCode
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        String studentCode = getStudentCode(user.getId());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .email(email)
                .studentCode(studentCode)
                .role(user.getRole() != null ? user.getRole().name() : null)
                .staffLevel(user.getStaffLevel())
                .build();
    }

    @Override
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        try {
            // 1. Verify Google ID token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken == null) {
                throw new RuntimeException("Google ID token không hợp lệ");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            // 2. Chỉ cho phép email @st.utc2.edu.vn
            if (!email.endsWith("@" + ALLOWED_DOMAIN)) {
                throw new RuntimeException("Chỉ tài khoản @" + ALLOWED_DOMAIN + " mới được đăng nhập");
            }

            // 3. Tìm hoặc tạo user
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = User.builder()
                        .email(email)
                        .password("") // Google login không cần password
                        .authProvider("GOOGLE")
                        .build();
                return userRepository.save(newUser);
            });

            // 4. Tạo JWT token
            var userDetails = userDetailsService.loadUserByUsername(email);
            String token = jwtService.generateToken(userDetails);

            // 5. Lấy studentCode
            String studentCode = getStudentCode(user.getId());

            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .email(email)
                    .studentCode(studentCode)
                    .role(user.getRole() != null ? user.getRole().name() : null)
                    .staffLevel(user.getStaffLevel())
                    .build();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xác thực Google: " + e.getMessage());
        }
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        // TODO: implement khi cần
        return null;
    }

    // Lấy MSSV từ student_profile — trả "" nếu chưa có (Google user mới)
    private String getStudentCode(Long userId) {
        try {
            Object result = entityManager
                    .createNativeQuery("SELECT student_code FROM student_profile WHERE user_id = :id")
                    .setParameter("id", userId)
                    .getSingleResult();
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }
}
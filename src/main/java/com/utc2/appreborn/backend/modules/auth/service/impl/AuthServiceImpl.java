package com.utc2.appreborn.backend.modules.auth.service.impl;

import com.utc2.appreborn.backend.modules.auth.service.AuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.auth.dto.AuthResponse;
import com.utc2.appreborn.backend.modules.auth.dto.ForgotPasswordRequest;
import com.utc2.appreborn.backend.modules.auth.dto.GoogleLoginRequest;
import com.utc2.appreborn.backend.modules.auth.dto.LoginRequest;
import com.utc2.appreborn.backend.modules.auth.dto.RegisterRequest;
import com.utc2.appreborn.backend.modules.auth.dto.ResetPasswordRequest;
import com.utc2.appreborn.backend.modules.auth.entity.PasswordResetToken;
import com.utc2.appreborn.backend.modules.auth.entity.User;
import com.utc2.appreborn.backend.modules.auth.repository.PasswordResetTokenRepository;
import com.utc2.appreborn.backend.modules.auth.repository.UserRepository;
import com.utc2.appreborn.backend.modules.auth.service.MailService;
import com.utc2.appreborn.backend.security.jwt.JwtService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${google.client.id:}")
    private String googleClientId;

    private static final String ALLOWED_DOMAIN = "st.utc2.edu.vn";
    private static final String STUDENT_DOMAIN = "@st.utc2.edu.vn";

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = request.getStudentCode().contains("@")
                ? request.getStudentCode()
                : request.getStudentCode() + STUDENT_DOMAIN;

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));

        var userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(userDetails);

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
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId.trim()))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken == null) {
                throw new RuntimeException("Google ID token không hợp lệ");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            if (!email.endsWith("@" + ALLOWED_DOMAIN)) {
                throw new RuntimeException("Chỉ tài khoản @" + ALLOWED_DOMAIN + " mới được đăng nhập");
            }

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = User.builder()
                        .email(email)
                        .password("")
                        .authProvider("GOOGLE")
                        .enabled(true)
                        .build();
                return userRepository.save(newUser);
            });

            var userDetails = userDetailsService.loadUserByUsername(email);
            String token = jwtService.generateToken(userDetails);
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
        return null;
    }

    // ─── Forgot Password ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Ghép domain nếu chỉ nhập MSSV
        String email = request.getEmail().contains("@")
                ? request.getEmail()
                : request.getEmail() + STUDENT_DOMAIN;

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email: " + email));

        // Xóa token cũ nếu có
        passwordResetTokenRepository.deleteAllByUserId(user.getId());

        // Sinh OTP 6 chữ số
        String otp = generateOtp();

        PasswordResetToken prt = PasswordResetToken.builder()
                .userId(user.getId())
                .token(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        passwordResetTokenRepository.save(prt);

        // Gửi email
        mailService.sendPasswordResetEmail(email, otp);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().contains("@")
                ? request.getEmail()
                : request.getEmail() + STUDENT_DOMAIN;

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        PasswordResetToken prt = passwordResetTokenRepository.findByToken(request.getOtp())
                .orElseThrow(() -> new RuntimeException("Mã OTP không hợp lệ"));

        if (prt.isUsed()) {
            throw new RuntimeException("Mã OTP đã được sử dụng");
        }
        if (prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn");
        }
        if (!prt.getUserId().equals(user.getId())) {
            throw new RuntimeException("Mã OTP không khớp với tài khoản");
        }

        // Cập nhật mật khẩu
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Đánh dấu token đã dùng
        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

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

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
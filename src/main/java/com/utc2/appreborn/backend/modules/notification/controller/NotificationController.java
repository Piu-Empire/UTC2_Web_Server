package com.utc2.appreborn.backend.modules.notification.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.notification.dto.OtpRequest;
import com.utc2.appreborn.backend.modules.notification.dto.VerifyOtpRequest;
import com.utc2.appreborn.backend.modules.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody OtpRequest request) {
        notificationService.sendOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("OTP đã gửi").data(null).build());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        notificationService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Xác thực thành công").data(null).build());
    }
}

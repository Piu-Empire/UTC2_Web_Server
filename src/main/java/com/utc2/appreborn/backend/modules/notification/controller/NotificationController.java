package com.utc2.appreborn.backend.modules.notification.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.notification.dto.*;
import com.utc2.appreborn.backend.modules.notification.service.NotificationService;
import com.utc2.appreborn.backend.modules.notification.service.NotificationSettingService;
import com.utc2.appreborn.backend.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationSettingService settingService;

    // ── OTP (giữ nguyên — public, không cần auth) ─────────────────

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

    // ── System Notifications (yêu cầu JWT) ───────────────────────

    /**
     * GET /api/v1/notifications?page=0&size=20
     * Lấy danh sách thông báo, mới nhất trước, phân trang.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = userDetails.getUser().getId();
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> result = notificationService.getNotifications(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/v1/notifications/unread-count
     * Đếm số thông báo chưa đọc — dùng cho badge.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }

    /**
     * PATCH /api/v1/notifications/{id}/read
     * Đánh dấu 1 thông báo là đã đọc.
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        Long userId = userDetails.getUser().getId();
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Đã đánh dấu đã đọc").data(null).build());
    }

    /**
     * PATCH /api/v1/notifications/read-all
     * Đánh dấu tất cả thông báo là đã đọc.
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        int updated = notificationService.markAllRead(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("updated", updated)));
    }

    /**
     * DELETE /api/v1/notifications/{id}
     * Xóa 1 thông báo.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        Long userId = userDetails.getUser().getId();
        notificationService.deleteNotification(userId, id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Đã xóa thông báo").data(null).build());
    }

    // ── Settings (yêu cầu JWT) ────────────────────────────────────

    /**
     * GET /api/v1/notifications/settings
     * Lấy cài đặt thông báo của user.
     */
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> getSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(ApiResponse.success(settingService.getSettings(userId)));
    }

    /**
     * PUT /api/v1/notifications/settings
     * Cập nhật cài đặt thông báo.
     */
    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> updateSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody NotificationSettingRequest request) {

        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(ApiResponse.success(settingService.updateSettings(userId, request)));
    }

    // ── FCM Token (yêu cầu JWT) ───────────────────────────────────

    /**
     * POST /api/v1/notifications/fcm/token
     * Đăng ký hoặc cập nhật FCM device token.
     */
    @PostMapping("/fcm/token")
    public ResponseEntity<ApiResponse<Void>> registerFcmToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FcmTokenRequest request) {

        Long userId = userDetails.getUser().getId();
        settingService.registerFcmToken(userId, request.getFcmToken());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("FCM token đã đăng ký").data(null).build());
    }

    /**
     * DELETE /api/v1/notifications/fcm/token
     * Xóa FCM token (logout hoặc user từ chối quyền notification).
     */
    @DeleteMapping("/fcm/token")
    public ResponseEntity<ApiResponse<Void>> removeFcmToken(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        settingService.removeFcmToken(userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("FCM token đã xóa").data(null).build());
    }

    // ── Test API (Dùng để test Push Notification) ─────────────────

    /**
     * POST /api/v1/notifications/test-push
     * Gửi 1 thông báo test (lưu DB + đẩy FCM) tới chính thiết bị của user đang login.
     */
    @PostMapping("/test-push")
    public ResponseEntity<ApiResponse<Void>> testPushNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String title,
            @RequestParam String body) {

        Long userId = userDetails.getUser().getId();
        notificationService.createSystemNotification(userId, "SYSTEM_TEST", title, body, null, null);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Đã gửi thông báo test thành công").data(null).build());
    }
}

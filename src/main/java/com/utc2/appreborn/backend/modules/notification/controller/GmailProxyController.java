package com.utc2.appreborn.backend.modules.notification.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.notification.dto.GmailLinkRequest;
import com.utc2.appreborn.backend.modules.notification.dto.GmailMessageResponse;
import com.utc2.appreborn.backend.modules.notification.dto.GmailStatusResponse;
import com.utc2.appreborn.backend.modules.notification.service.GmailProxyService;
import com.utc2.appreborn.backend.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GmailProxyController
 * ─────────────────────────────────────────────────────────────────
 * Tất cả endpoints yêu cầu JWT Bearer token.
 * Server làm proxy: nhận Google access token từ App → gọi Gmail API → trả kết quả.
 * Token lưu tạm thời (mã hóa AES), không lưu refresh token.
 */
@RestController
@RequestMapping("/api/v1/notifications/gmail")
@RequiredArgsConstructor
public class GmailProxyController {

    private final GmailProxyService gmailProxyService;

    /**
     * POST /api/v1/notifications/gmail/link
     * Liên kết Gmail — App gửi Google access token, server lưu mã hóa.
     */
    @PostMapping("/link")
    public ResponseEntity<ApiResponse<Void>> linkGmail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GmailLinkRequest request) {

        Long userId = userDetails.getUser().getId();
        gmailProxyService.linkGmail(userId, request.getGoogleAccessToken());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Đã liên kết Gmail thành công").data(null).build());
    }

    /**
     * DELETE /api/v1/notifications/gmail/unlink
     * Hủy liên kết Gmail — xóa token khỏi DB.
     */
    @DeleteMapping("/unlink")
    public ResponseEntity<ApiResponse<Void>> unlinkGmail(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        gmailProxyService.unlinkGmail(userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Đã hủy liên kết Gmail").data(null).build());
    }

    /**
     * GET /api/v1/notifications/gmail/status
     * Kiểm tra trạng thái liên kết Gmail.
     * App dùng để quyết định hiển thị nút "Kích hoạt" hay "Kết nối lại".
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<GmailStatusResponse>> getGmailStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        GmailStatusResponse status = gmailProxyService.getStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    /**
     * GET /api/v1/notifications/gmail/inbox
     * Lấy tối đa 20 email gần nhất từ Gmail qua server proxy.
     * Trả 400 nếu token hết hạn (App cần kết nối lại).
     */
    @GetMapping("/inbox")
    public ResponseEntity<ApiResponse<List<GmailMessageResponse>>> getInbox(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        List<GmailMessageResponse> inbox = gmailProxyService.getInbox(userId);
        return ResponseEntity.ok(ApiResponse.success(inbox));
    }
}

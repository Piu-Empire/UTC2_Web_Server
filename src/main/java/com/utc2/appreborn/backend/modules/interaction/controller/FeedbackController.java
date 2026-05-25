package com.utc2.appreborn.backend.modules.interaction.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.interaction.dto.*;
import com.utc2.appreborn.backend.modules.interaction.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interaction")
@RequiredArgsConstructor
@Tag(name = "Interaction", description = "API phản hồi & báo lỗi")
@SecurityRequirement(name = "bearerAuth")
public class FeedbackController {

    private final FeedbackService feedbackService;

    // ── Student endpoints ─────────────────────────────────────────────

    /** POST /api/v1/interaction/feedback — sinh viên gửi phản hồi */
    @PostMapping("/feedback")
    @Operation(summary = "Gửi phản hồi hoặc báo lỗi")
    public ResponseEntity<ApiResponse<FeedbackResponse>> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FeedbackRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        feedbackService.submit(userDetails.getUsername(), request)));
    }

    /** GET /api/v1/interaction/feedback/my — lịch sử của sinh viên */
    @GetMapping("/feedback/my")
    @Operation(summary = "Lịch sử phản hồi của tôi")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> myFeedbacks(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success(
                feedbackService.getMyFeedbacks(userDetails.getUsername())));
    }

    // ── Admin endpoints ───────────────────────────────────────────────

    /**
     * GET /api/v1/interaction/feedback/all
     * Query param: status (optional) — "chưa đọc" | "đã đọc" | "đã phản hồi"
     */
    @GetMapping("/feedback/all")
    @Operation(summary = "Admin: xem tất cả phản hồi")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getAll(
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(ApiResponse.success(
                feedbackService.getAll(status)));
    }

    /**
     * PUT /api/v1/interaction/feedback/{id}/reply
     * Body: { adminReply }
     */
    @PutMapping("/feedback/{id}/reply")
    @Operation(summary = "Admin: gửi phản hồi cho sinh viên")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeedbackResponse>> reply(
            @PathVariable Long id,
            @Valid @RequestBody AdminReplyRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                feedbackService.reply(id, request.getAdminReply())));
    }

    /**
     * PATCH /api/v1/interaction/feedback/{id}/status
     * Body: { status }
     */
    @PatchMapping("/feedback/{id}/status")
    @Operation(summary = "Admin: cập nhật trạng thái phản hồi")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeedbackResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminStatusRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                feedbackService.updateStatus(id, request.getStatus())));
    }
}
package com.utc2.appreborn.backend.modules.finance.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.finance.dto.TuitionResponse;
import com.utc2.appreborn.backend.modules.finance.dto.TuitionSummaryResponse;
import com.utc2.appreborn.backend.modules.finance.service.TuitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tuition")
@RequiredArgsConstructor
@Tag(name = "Tuition", description = "API học phí sinh viên")
@SecurityRequirement(name = "bearerAuth")
public class TuitionController {

    private final TuitionService tuitionService;

    @GetMapping("/summary")
    @Operation(summary = "Tổng quan học phí (tổng nợ + tất cả kỳ)")
    public ResponseEntity<ApiResponse<TuitionSummaryResponse>> getSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                tuitionService.getMyTuitionSummary(userDetails.getUsername())));
    }

    @GetMapping("/history")
    @Operation(summary = "Toàn bộ lịch sử học phí (cả chưa đóng)")
    public ResponseEntity<ApiResponse<List<TuitionResponse>>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                tuitionService.getMyTuitionHistory(userDetails.getUsername())));
    }

    /** Dùng cho Invoice screen — chỉ trả kỳ đã đóng đủ */
    @GetMapping("/paid")
    @Operation(summary = "Lịch sử các kỳ đã đóng đủ (Invoice screen)")
    public ResponseEntity<ApiResponse<List<TuitionResponse>>> getPaidHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                tuitionService.getMyPaidHistory(userDetails.getUsername())));
    }

    @GetMapping("/semester/{semester}")
    @Operation(summary = "Học phí theo kỳ học")
    public ResponseEntity<ApiResponse<TuitionResponse>> getBySemester(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String semester) {
        return ResponseEntity.ok(ApiResponse.success(
                tuitionService.getTuitionBySemester(userDetails.getUsername(), semester)));
    }

    /**
     * POST /api/v1/tuition/pay/{semesterId}
     * Đóng toàn bộ học phí còn lại của 1 kỳ — chỉ hỗ trợ đóng 1 lần đủ.
     * App gọi sau khi người dùng xác nhận QR thanh toán thành công.
     *
     * @param paymentMethod optional, default "online" (query param)
     */
    @PostMapping("/pay/{semesterId}")
    @Operation(summary = "Thanh toán học phí 1 kỳ (đóng đủ 1 lần)")
    public ResponseEntity<ApiResponse<TuitionResponse>> pay(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long semesterId,
            @RequestParam(required = false, defaultValue = "online") String paymentMethod) {
        return ResponseEntity.ok(ApiResponse.success(
                tuitionService.payTuition(userDetails.getUsername(), semesterId, paymentMethod)));
    }
}
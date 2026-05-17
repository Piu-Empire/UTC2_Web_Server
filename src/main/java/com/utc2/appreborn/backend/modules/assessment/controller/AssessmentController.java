package com.utc2.appreborn.backend.modules.assessment.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.assessment.dto.*;
import com.utc2.appreborn.backend.modules.assessment.entity.AssessmentPeriod;
import com.utc2.appreborn.backend.modules.assessment.service.AssessmentService;
import com.utc2.appreborn.backend.modules.auth.entity.User;
import com.utc2.appreborn.backend.modules.auth.repository.UserRepository;
import com.utc2.appreborn.backend.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assessment")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final UserRepository    userRepository;

    // ─── Học kỳ ───────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/assessment/periods
     * App lấy danh sách học kỳ để hiển thị dropdown.
     */
    @GetMapping("/periods")
    public ResponseEntity<ApiResponse<List<AssessmentPeriod>>> getPeriods() {
        return ResponseEntity.ok(ApiResponse.success(assessmentService.getPeriods()));
    }

    // ─── Sinh viên tự đánh giá (App → Server) ────────────────────────────────

    /**
     * POST /api/v1/assessment/student
     * App gửi dữ liệu sinh viên tự đánh giá rèn luyện.
     * Luồng: App → Server → DB (Admin chỉ xem, không sửa)
     */
    @PostMapping("/student")
    public ResponseEntity<ApiResponse<Void>> saveStudentAssessment(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody SaveStudentAssessmentRequest request) {

        Long userId = resolveUserId(principal);
        assessmentService.saveStudentAssessment(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * GET /api/v1/assessment/student?periodId=HK1_2025_2026
     * App lấy lại dữ liệu đã lưu (để hiển thị khi mở lại màn hình).
     */
    @GetMapping("/student")
    public ResponseEntity<ApiResponse<StudentAssessmentResponse>> getStudentAssessment(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam String periodId) {

        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(ApiResponse.success(
                assessmentService.getStudentAssessment(userId, periodId)));
    }

    // ─── Đánh giá CVHT (App → Server) ────────────────────────────────────────

    /**
     * POST /api/v1/assessment/advisor
     * App gửi dữ liệu sinh viên đánh giá CVHT.
     * Luồng: App → Server → DB (Admin chỉ xem, không sửa)
     */
    @PostMapping("/advisor")
    public ResponseEntity<ApiResponse<Void>> saveAdvisorAssessment(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody SaveAdvisorAssessmentRequest request) {

        Long userId = resolveUserId(principal);
        assessmentService.saveAdvisorAssessment(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ─── Điểm external (Admin → Server → App) ────────────────────────────────

    /**
     * POST /api/v1/assessment/external/import
     * Admin import điểm Tập thể lớp / Khoa/BM / Trường cho sinh viên.
     * Luồng: Admin → Server → DB → App (App chỉ đọc, không sửa)
     */
    @PostMapping("/external/import")
    public ResponseEntity<ApiResponse<Void>> importExternal(
            @RequestBody ImportExternalAssessmentRequest request) {

        assessmentService.importExternalAssessment(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * GET /api/v1/assessment/external?periodId=HK1_2025_2026
     * App lấy điểm readonly Tập thể lớp / Khoa/BM / Trường.
     * Luồng: Admin → Server → App (App chỉ đọc)
     */
    @GetMapping("/external")
    public ResponseEntity<ApiResponse<ExternalAssessmentResponse>> getExternal(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam String periodId) {

        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(ApiResponse.success(
                assessmentService.getExternalAssessment(userId, periodId)));
    }

    // ─── Admin: xem toàn bộ ──────────────────────────────────────────────────

    /**
     * GET /api/v1/assessment/admin/student?periodId=HK1_2025_2026
     * Admin xem toàn bộ đánh giá sinh viên theo học kỳ (để export CSV/Word).
     */
    @GetMapping("/admin/student")
    public ResponseEntity<ApiResponse<List<StudentAssessmentResponse>>> adminGetAllStudent(
            @RequestParam String periodId) {

        return ResponseEntity.ok(ApiResponse.success(
                assessmentService.getAllStudentAssessments(periodId)));
    }

    /**
     * GET /api/v1/assessment/admin/advisor?periodId=HK1_2025_2026
     * Admin xem toàn bộ đánh giá CVHT theo học kỳ (để export CSV/Word).
     */
    @GetMapping("/admin/advisor")
    public ResponseEntity<ApiResponse<List<StudentAssessmentResponse>>> adminGetAllAdvisor(
            @RequestParam String periodId) {

        return ResponseEntity.ok(ApiResponse.success(
                assessmentService.getAllAdvisorAssessments(periodId)));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    /**
     * Lấy userId từ JWT principal.
     * CustomUserDetails dùng email làm username → tìm user theo email → lấy id.
     */
    private Long resolveUserId(CustomUserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + principal.getUsername()));
        return user.getId();
    }
}
package com.utc2.appreborn.backend.modules.assessment.controller;

import com.utc2.appreborn.backend.common.enums.Role;
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

    @GetMapping("/periods")
    public ResponseEntity<ApiResponse<List<AssessmentPeriod>>> getPeriods() {
        return ResponseEntity.ok(ApiResponse.success(assessmentService.getPeriods()));
    }

    // ─── Sinh viên tự đánh giá ────────────────────────────────────────────────

    @PostMapping("/student")
    public ResponseEntity<ApiResponse<Void>> saveStudentAssessment(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody SaveStudentAssessmentRequest request) {
        assessmentService.saveStudentAssessment(resolveUserId(principal), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/student")
    public ResponseEntity<ApiResponse<StudentAssessmentResponse>> getStudentAssessment(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam String periodId) {
        return ResponseEntity.ok(ApiResponse.success(
                assessmentService.getStudentAssessment(resolveUserId(principal), periodId)));
    }

    // ─── Đánh giá CVHT ───────────────────────────────────────────────────────

    @PostMapping("/advisor")
    public ResponseEntity<ApiResponse<Void>> saveAdvisorAssessment(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody SaveAdvisorAssessmentRequest request) {
        assessmentService.saveAdvisorAssessment(resolveUserId(principal), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/advisor")
    public ResponseEntity<ApiResponse<AdvisorAssessmentResponse>> getAdvisorAssessment(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam String periodId) {
        return ResponseEntity.ok(ApiResponse.success(
                assessmentService.getAdvisorAssessment(resolveUserId(principal), periodId)));
    }

    // ─── External: App đọc (readonly) ─────────────────────────────────────────

    @GetMapping("/external")
    public ResponseEntity<ApiResponse<ExternalAssessmentResponse>> getExternal(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam String periodId) {
        return ResponseEntity.ok(ApiResponse.success(
                assessmentService.getExternalAssessment(resolveUserId(principal), periodId)));
    }

    // ─── External: Web Admin nhập điểm theo role ──────────────────────────────

    @PutMapping("/external/tap-the")
    public ResponseEntity<ApiResponse<Void>> setTapThe(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody SetExternalScoreRequest request) {
        checkRole(principal, Role.STAFF, 1);
        assessmentService.setTapTheScore(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/external/bo-mon")
    public ResponseEntity<ApiResponse<Void>> setBoMon(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody SetExternalScoreRequest request) {
        checkRole(principal, Role.STAFF, 3);
        assessmentService.setBoMonScore(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/external/khoa")
    public ResponseEntity<ApiResponse<Void>> setKhoa(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody SetExternalScoreRequest request) {
        checkRole(principal, Role.STAFF, 4);
        assessmentService.setKhoaScore(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/external/truong")
    public ResponseEntity<ApiResponse<Void>> setTruong(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody SetExternalScoreRequest request) {
        checkRoleTruong(principal);
        assessmentService.setTruongScore(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ─── External: Duyệt ─────────────────────────────────────────────────────

    @PostMapping("/external/approve/advisor")
    public ResponseEntity<ApiResponse<Void>> approveAdvisor(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody ApproveRequest request) {
        checkAdvisor(principal);
        assessmentService.approveAdvisor(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/external/approve/khoa")
    public ResponseEntity<ApiResponse<Void>> approveKhoa(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody ApproveRequest request) {
        checkRole(principal, Role.STAFF, 4);
        assessmentService.approveKhoa(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/external/approve/truong")
    public ResponseEntity<ApiResponse<Void>> approveTruong(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody ApproveRequest request) {
        checkRoleTruong(principal);
        assessmentService.approveTruong(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ─── Admin xem toàn bộ ───────────────────────────────────────────────────

    @GetMapping("/admin/student")
    public ResponseEntity<ApiResponse<List<StudentAssessmentResponse>>> adminGetAllStudent(
            @RequestParam String periodId) {
        return ResponseEntity.ok(ApiResponse.success(
                assessmentService.getAllStudentAssessments(periodId)));
    }

    @GetMapping("/admin/advisor")
    public ResponseEntity<ApiResponse<List<AdvisorAssessmentResponse>>> adminGetAllAdvisor(
            @RequestParam String periodId) {
        return ResponseEntity.ok(ApiResponse.success(
                assessmentService.getAllAdvisorAssessments(periodId)));
    }

    @GetMapping("/admin/overview")
    public ResponseEntity<ApiResponse<List<StudentOverviewResponse>>> adminOverview(
            @RequestParam String periodId) {
        return ResponseEntity.ok(ApiResponse.success(
                assessmentService.getStudentOverview(periodId)));
    }

    /** Legacy import — ADMIN only */
    @PostMapping("/external/import")
    public ResponseEntity<ApiResponse<Void>> importExternal(
            @RequestBody ImportExternalAssessmentRequest request) {
        assessmentService.importExternalAssessment(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Long resolveUserId(CustomUserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + principal.getUsername()))
                .getId();
    }

    private User resolveUser(CustomUserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + principal.getUsername()));
    }

    private void checkRole(CustomUserDetails principal, Role role, int level) {
        User user = resolveUser(principal);
        if (user.getRole() == Role.ADMIN) return;
        if (user.getRole() != role || !Integer.valueOf(level).equals(user.getStaffLevel())) {
            throw new RuntimeException("Không có quyền thực hiện thao tác này");
        }
    }

    private void checkRoleTruong(CustomUserDetails principal) {
        User user = resolveUser(principal);
        if (user.getRole() == Role.ADMIN) return;
        if (user.getRole() != Role.STAFF || !Integer.valueOf(5).equals(user.getStaffLevel())) {
            throw new RuntimeException("Không có quyền thực hiện thao tác này");
        }
    }

    private void checkAdvisor(CustomUserDetails principal) {
        User user = resolveUser(principal);
        if (user.getRole() == Role.ADMIN) return;
        if (user.getRole() != Role.ADVISOR) {
            throw new RuntimeException("Không có quyền thực hiện thao tác này");
        }
    }
}
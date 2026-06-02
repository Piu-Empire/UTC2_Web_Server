package com.utc2.appreborn.backend.modules.academic.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.academic.dto.*;
import com.utc2.appreborn.backend.modules.academic.service.AcademicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic")
@RequiredArgsConstructor
public class AcademicController {

    private final AcademicService academicService;

    // ── Student / App (GET) ───────────────────────────────────────────────

    @GetMapping("/semesters")
    public ResponseEntity<ApiResponse<List<SemesterDto>>> getSemesters(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success(academicService.getSemesters(userId)));
    }

    @GetMapping("/grades")
    public ResponseEntity<ApiResponse<List<CourseGradeDto>>> getGrades(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long semesterId) {
        return ResponseEntity.ok(ApiResponse.success(academicService.getGrades(userId, semesterId)));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDto>>> getLeaderboard(
            @RequestParam(required = false) Long   semesterId,
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.getLeaderboard(semesterId, academicYear)));
    }

    @GetMapping("/scholarships")
    public ResponseEntity<ApiResponse<List<ScholarshipDto>>> getScholarships(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success(academicService.getScholarships(userId)));
    }

    @GetMapping("/warnings")
    public ResponseEntity<ApiResponse<List<AcademicWarningDto>>> getWarnings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long semesterId) {
        return ResponseEntity.ok(ApiResponse.success(academicService.getWarnings(userId, semesterId)));
    }

    // ── STAFF lv2: Giảng viên nhập điểm theo môn + lớp ──────────────────

    /** Lấy danh sách sinh viên theo courseId + className để nhập điểm hàng loạt */
    @GetMapping("/grades/by-course")
    public ResponseEntity<ApiResponse<List<GradesByCourseDto>>> getGradesByCourse(
            @RequestParam Long courseId,
            @RequestParam(required = false) String className) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.getGradesByCourse(courseId, className)));
    }

    /** Nhập/cập nhật điểm 1 enrollment */
    @PutMapping("/grades/{enrollmentId}")
    public ResponseEntity<ApiResponse<CourseGradeDto>> updateGrade(
            @PathVariable Long enrollmentId,
            @RequestBody GradeUpdateDto dto) {
        return ResponseEntity.ok(ApiResponse.success(academicService.updateGrade(enrollmentId, dto)));
    }

    // ── ADVISOR: Cố vấn học tập quản lý warning + scholarship ────────────

    @PostMapping("/warnings")
    public ResponseEntity<ApiResponse<AcademicWarningDto>> upsertWarning(
            @RequestBody WarningUpsertDto dto) {
        return ResponseEntity.ok(ApiResponse.success(academicService.upsertWarning(dto)));
    }

    @DeleteMapping("/warnings/{warningId}")
    public ResponseEntity<ApiResponse<Void>> deleteWarning(@PathVariable Long warningId) {
        academicService.deleteWarning(warningId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/scholarships/status")
    public ResponseEntity<ApiResponse<ScholarshipDto>> updateScholarshipStatus(
            @RequestBody ScholarshipUpsertDto dto) {
        return ResponseEntity.ok(ApiResponse.success(academicService.updateScholarshipStatus(dto)));
    }
}
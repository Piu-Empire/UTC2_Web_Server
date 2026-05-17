package com.utc2.appreborn.backend.modules.academic.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.academic.dto.*;
import com.utc2.appreborn.backend.modules.academic.service.AcademicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AcademicController — Kết quả học tập
 *
 * Base: /api/v1/academic
 * Tất cả endpoint yêu cầu JWT (xử lý bởi JwtFilter + SecurityConfig).
 *
 * Endpoints:
 *  GET /semesters                            → danh sách kỳ học
 *  GET /grades?semesterId={id}               → bảng điểm (optional filter)
 *  GET /leaderboard?semesterId={id}          → xếp hạng theo kỳ
 *  GET /leaderboard?academicYear={year}      → xếp hạng theo năm học
 *  GET /scholarships                         → học bổng + trạng thái
 *  GET /warnings?semesterId={id}             → cảnh báo học vụ (optional filter)
 */
@RestController
@RequestMapping("/api/v1/academic")
@RequiredArgsConstructor
public class AcademicController {

    private final AcademicService academicService;

    @GetMapping("/semesters")
    public ResponseEntity<ApiResponse<List<SemesterDto>>> getSemesters() {
        return ResponseEntity.ok(ApiResponse.success(academicService.getSemesters()));
    }

    @GetMapping("/grades")
    public ResponseEntity<ApiResponse<List<CourseGradeDto>>> getGrades(
            @RequestParam(required = false) Long semesterId) {
        return ResponseEntity.ok(ApiResponse.success(academicService.getGrades(semesterId)));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDto>>> getLeaderboard(
            @RequestParam(required = false) Long   semesterId,
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.getLeaderboard(semesterId, academicYear)));
    }

    @GetMapping("/scholarships")
    public ResponseEntity<ApiResponse<List<ScholarshipDto>>> getScholarships() {
        return ResponseEntity.ok(ApiResponse.success(academicService.getScholarships()));
    }

    @GetMapping("/warnings")
    public ResponseEntity<ApiResponse<List<AcademicWarningDto>>> getWarnings(
            @RequestParam(required = false) Long semesterId) {
        return ResponseEntity.ok(ApiResponse.success(academicService.getWarnings(semesterId)));
    }
}
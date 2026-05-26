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
 *
 * userId (optional):
 *   - Không truyền → dùng token của user đang login (App mobile)
 *   - Truyền vào   → xem data của user đó (Admin web)
 */
/**
 * AcademicController — Kết quả học tập
 *
 * Base: /api/v1/academic
 *
 * Endpoints:
 *  GET /semesters              → danh sách kỳ học của sinh viên
 *  GET /grades?semesterId=     → điểm các môn (null = tất cả kỳ)
 *  GET /leaderboard?semesterId=&academicYear= → bảng xếp hạng GPA
 *  GET /scholarships           → danh sách học bổng + trạng thái
 *  GET /warnings?semesterId=   → cảnh báo học vụ (null = tất cả kỳ)
 */
@RestController
@RequestMapping("/api/v1/academic")
@RequiredArgsConstructor
public class AcademicController {

    private final AcademicService academicService;

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
}

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

    // ── Student / App ─────────────────────────────────────────────────────

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

    // ── STAFF lv2: Nhập điểm ─────────────────────────────────────────────

    @GetMapping("/grades/by-course")
    public ResponseEntity<ApiResponse<List<GradesByCourseDto>>> getGradesByCourse(
            @RequestParam Long courseId,
            @RequestParam(required = false) String className) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.getGradesByCourse(courseId, className)));
    }

    @PutMapping("/grades/{enrollmentId}")
    public ResponseEntity<ApiResponse<CourseGradeDto>> updateGrade(
            @PathVariable Long enrollmentId,
            @RequestBody GradeUpdateDto dto) {
        return ResponseEntity.ok(ApiResponse.success(academicService.updateGrade(enrollmentId, dto)));
    }

    // ── ADVISOR + lv5: Import warning & scholarship ───────────────────────

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

    // ── lv5 + ADMIN: Duyệt ───────────────────────────────────────────────

    /** Duyệt bảng xếp hạng theo kỳ */
    @PostMapping("/leaderboard/approve")
    public ResponseEntity<ApiResponse<Void>> approveLeaderboard(@RequestParam Long semesterId) {
        academicService.approveLeaderboard(semesterId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/leaderboard/approve")
    public ResponseEntity<ApiResponse<Void>> revokeLeaderboard(@RequestParam Long semesterId) {
        academicService.revokeLeaderboard(semesterId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** Xem trước bảng xếp hạng chưa duyệt (admin/lv5) */
    @GetMapping("/leaderboard/pending")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDto>>> getPendingLeaderboard(
            @RequestParam(required = false) Long   semesterId,
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.getPendingLeaderboard(semesterId, academicYear)));
    }

    /** Duyệt cảnh báo học vụ */
    @PostMapping("/warnings/{warningId}/approve")
    public ResponseEntity<ApiResponse<AcademicWarningDto>> approveWarning(@PathVariable Long warningId) {
        return ResponseEntity.ok(ApiResponse.success(academicService.approveWarning(warningId)));
    }

    /** Danh sách cảnh báo chờ duyệt */
    @GetMapping("/warnings/pending")
    public ResponseEntity<ApiResponse<List<AcademicWarningDto>>> getPendingWarnings() {
        return ResponseEntity.ok(ApiResponse.success(academicService.getPendingWarnings()));
    }

    /** Duyệt học bổng (pending → approved, hiển thị app) */
    @PostMapping("/scholarships/approve")
    public ResponseEntity<ApiResponse<ScholarshipDto>> approveScholarship(
            @RequestParam Long userId,
            @RequestParam Long scholarshipId) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.approveScholarship(userId, scholarshipId)));
    }

    /** Chuyển học bổng sang đã nhận */
    @PostMapping("/scholarships/received")
    public ResponseEntity<ApiResponse<ScholarshipDto>> markScholarshipReceived(
            @RequestParam Long userId,
            @RequestParam Long scholarshipId) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.markScholarshipReceived(userId, scholarshipId)));
    }

    /** Danh sách học bổng chờ duyệt */
    @GetMapping("/scholarships/pending")
    public ResponseEntity<ApiResponse<List<Object>>> getPendingScholarships() {
        return ResponseEntity.ok(ApiResponse.success(academicService.getPendingScholarships()));
    }

    // ── Teacher course ────────────────────────────────────────────────────

    @GetMapping("/teacher-courses")
    public ResponseEntity<ApiResponse<List<TeacherCourseDto>>> getMyTeacherCourses() {
        return ResponseEntity.ok(ApiResponse.success(academicService.getMyTeacherCourses()));
    }

    @PostMapping("/teacher-courses")
    public ResponseEntity<ApiResponse<TeacherCourseDto>> assignTeacher(
            @RequestParam Long userId, @RequestParam Long courseId,
            @RequestParam Long semesterId,
            @RequestParam(required = false) String className) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.assignTeacher(userId, courseId, semesterId, className)));
    }

    @DeleteMapping("/teacher-courses/{id}")
    public ResponseEntity<ApiResponse<Void>> removeTeacherCourse(@PathVariable Long id) {
        academicService.removeTeacherCourse(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
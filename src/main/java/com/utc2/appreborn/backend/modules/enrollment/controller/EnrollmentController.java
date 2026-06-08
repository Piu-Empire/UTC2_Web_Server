package com.utc2.appreborn.backend.modules.enrollment.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.enrollment.dto.CourseItemDto;
import com.utc2.appreborn.backend.modules.enrollment.dto.EnrollRequest;
import com.utc2.appreborn.backend.modules.enrollment.dto.EnrollmentItemDto;
import com.utc2.appreborn.backend.modules.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * EnrollmentController — Đăng ký học phần
 *
 * Base: /api/v1/enrollment
 *
 * Endpoints:
 *  GET    /my           → danh sách môn đã đăng ký + điểm (cần token)
 *  GET    /courses      → danh sách môn có thể đăng ký (cần token)
 *  POST   /             → đăng ký 1 môn học (cần token)
 *  DELETE /{id}         → hủy đăng ký (cần token)
 */
@RestController
@RequestMapping("/api/v1/enrollment")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * GET /api/v1/enrollment/my
     * Lấy toàn bộ môn đã đăng ký + điểm của sinh viên đang đăng nhập.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<EnrollmentItemDto>>> getMyEnrollments() {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.getMyEnrollments()));
    }

    /**
     * GET /api/v1/enrollment/courses
     * Danh sách tất cả môn học, đánh dấu môn đã đăng ký.
     */
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<CourseItemDto>>> getAvailableCourses() {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.getAvailableCourses()));
    }

    /**
     * POST /api/v1/enrollment
     * Đăng ký 1 môn học.
     * Body: { "courseId": 1, "semesterId": 1 }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentItemDto>> enroll(
            @Valid @RequestBody EnrollRequest request) {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.enroll(request)));
    }

    /**
     * DELETE /api/v1/enrollment/{enrollmentId}
     * Hủy đăng ký 1 môn học.
     */
    @DeleteMapping("/{enrollmentId}")
    public ResponseEntity<ApiResponse<Void>> cancelEnrollment(
            @PathVariable Long enrollmentId) {
        enrollmentService.cancelEnrollment(enrollmentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

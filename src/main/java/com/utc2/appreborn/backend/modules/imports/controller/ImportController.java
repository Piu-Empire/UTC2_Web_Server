package com.utc2.appreborn.backend.modules.imports.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.imports.dto.ImportResultResponse;
import com.utc2.appreborn.backend.modules.imports.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/import")
@RequiredArgsConstructor
@Tag(name = "Admin - Import", description = "Import dữ liệu từ file CSV/Excel")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class ImportController {

    private final ImportService importService;

    @PostMapping(value = "/profiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import profile sinh viên")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importProfiles(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        return ResponseEntity.ok(ApiResponse.success(
                importService.importProfiles(file, overwrite)));
    }

    @PostMapping(value = "/fees", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import học phí")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importFees(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        return ResponseEntity.ok(ApiResponse.success(
                importService.importTuition(file, overwrite)));
    }

    @PostMapping(value = "/curriculum", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import chương trình đào tạo")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importCurriculum(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        return ResponseEntity.ok(ApiResponse.success(
                importService.importCurriculum(file, overwrite)));
    }

    @PostMapping(value = "/courses", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import học phần")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importCourses(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        return ResponseEntity.ok(ApiResponse.success(
                importService.importCourses(file, overwrite)));
    }

    @PostMapping(value = "/students", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import sinh viên")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importStudents(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        return ResponseEntity.ok(ApiResponse.success(
                importService.importStudents(file, overwrite)));
    }

    /**
     * POST /api/v1/admin/import/dormitory-rooms
     * Required cols: room_code, building, capacity, room_type, price_per_month
     * Optional cols: floor, status (mặc định: available), amenities
     */
    @PostMapping(value = "/dormitory-rooms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import phòng ký túc xá")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importDormitoryRooms(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        return ResponseEntity.ok(ApiResponse.success(
                importService.importDormitoryRooms(file, overwrite)));
    }

    /**
     * POST /api/v1/admin/import/enrollments
     * Required cols: student_code, course_code, semester_id
     * Optional cols: status, midterm_score, final_score, assignment_score,
     *                total_score, letter_grade, grade_point, is_passed
     */
    @PostMapping(value = "/enrollments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import đăng ký học phần & điểm")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importEnrollments(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        return ResponseEntity.ok(ApiResponse.success(
                importService.importEnrollments(file, overwrite)));
    }
}
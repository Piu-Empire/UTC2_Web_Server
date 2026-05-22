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

    /**
     * POST /api/v1/admin/import/profiles
     * Import/cập nhật profile sinh viên
     * Required cols: student_code
     * Optional cols: full_name, phone_number, date_of_birth, gender, address,
     *                faculty, major, academic_year, class_name, status
     */
    @PostMapping(value = "/profiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import profile sinh viên")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importProfiles(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        return ResponseEntity.ok(ApiResponse.success(
                importService.importProfiles(file, overwrite)));
    }

    /**
     * POST /api/v1/admin/import/fees
     * Import học phí
     * Required cols: student_code, semester_id, total_amount
     * Optional cols: paid_amount, due_date, payment_method
     */
    @PostMapping(value = "/fees", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import học phí")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importFees(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        return ResponseEntity.ok(ApiResponse.success(
                importService.importTuition(file, overwrite)));
    }

    /**
     * POST /api/v1/admin/import/curriculum
     * Import chương trình đào tạo
     * Required cols: major, academic_year, course_code, semester_suggestion
     * Optional cols: is_required, group_name
     */
    @PostMapping(value = "/curriculum", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import chương trình đào tạo")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importCurriculum(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        return ResponseEntity.ok(ApiResponse.success(
                importService.importCurriculum(file, overwrite)));
    }
}
package com.utc2.appreborn.backend.modules.export.controller;

import com.utc2.appreborn.backend.modules.export.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

/**
 * ExcelExportController — Xuất file Excel
 *
 * Base: /api/v1/export
 *
 * Endpoints:
 *  GET /enrollment   → Xuất danh sách đăng ký học phần tất cả sinh viên
 *  GET /dormitory    → Xuất danh sách đăng ký KTX tất cả sinh viên
 */
@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
public class ExcelExportController {

    private final ExcelExportService excelExportService;

    /**
     * GET /api/v1/export/enrollment
     * Tải file Excel danh sách đăng ký học phần của toàn trường.
     */
    @GetMapping("/enrollment")
    public ResponseEntity<byte[]> exportEnrollments() throws IOException {
        byte[] data = excelExportService.exportEnrollments();
        String filename = "dang-ky-hoc-phan-" + LocalDate.now() + ".xlsx";
        return buildResponse(data, filename);
    }

    /**
     * GET /api/v1/export/dormitory
     * Tải file Excel danh sách đăng ký KTX của toàn trường.
     */
    @GetMapping("/dormitory")
    public ResponseEntity<byte[]> exportDormRegistrations() throws IOException {
        byte[] data = excelExportService.exportDormRegistrations();
        String filename = "dang-ky-ktx-" + LocalDate.now() + ".xlsx";
        return buildResponse(data, filename);
    }

    private ResponseEntity<byte[]> buildResponse(byte[] data, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(data.length))
                .body(data);
    }
}
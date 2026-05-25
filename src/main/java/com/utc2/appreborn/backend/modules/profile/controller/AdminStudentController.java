package com.utc2.appreborn.backend.modules.profile.controller;

import com.utc2.appreborn.backend.modules.profile.dto.StudentSummaryResponse;
import com.utc2.appreborn.backend.modules.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
// ĐÃ SỬA: Thêm "/api/v1" cho đúng chuẩn URL mà React đang gọi sang
@RequestMapping("/api/v1/admin/students")
@RequiredArgsConstructor
public class AdminStudentController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<Page<StudentSummaryResponse>> getStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String faculty,
            @RequestParam(required = false) String cohort,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StudentSummaryResponse> result = profileService.listStudents(
                    search, faculty, cohort, status, pageable
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Ép hệ thống phải in lỗi thật sự ra Log Docker nếu có lỗi logic, mapping dữ liệu ngầm
            System.out.println("=== LỖI PHÁT SINH TẠI ADMIN STUDENT CONTROLLER ===");
            e.printStackTrace();
            throw e;
        }
    }
}
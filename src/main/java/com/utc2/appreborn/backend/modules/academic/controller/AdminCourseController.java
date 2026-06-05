package com.utc2.appreborn.backend.modules.academic.controller;

import com.utc2.appreborn.backend.common.constants.ApiConstant;
import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.academic.entity.CourseEntity;
import com.utc2.appreborn.backend.modules.academic.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstant.API_PREFIX + "/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseRepository courseRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseEntity>>> getAllCourses() {
        // Dùng đúng CourseRepository đã có trong repo của bạn
        List<CourseEntity> courses = courseRepository.findAll();
        return ResponseEntity.ok(ApiResponse.<List<CourseEntity>>builder()
                .success(true)
                .message("Lấy danh sách môn học thành công")
                .data(courses)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseEntity>> getCourseById(@PathVariable Long id) {
        return courseRepository.findById(id)
                .map(course -> ResponseEntity.ok(ApiResponse.<CourseEntity>builder()
                        .success(true)
                        .data(course)
                        .build()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
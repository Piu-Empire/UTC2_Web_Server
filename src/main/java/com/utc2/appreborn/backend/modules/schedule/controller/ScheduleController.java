package com.utc2.appreborn.backend.modules.schedule.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.schedule.dto.ScheduleFileDto;
import com.utc2.appreborn.backend.modules.schedule.dto.ScheduleMetaDto;
import com.utc2.appreborn.backend.modules.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * GET /api/v1/schedule/meta?studentCode=2211020001
     *
     * Public — không cần token.
     * App gọi mỗi lần mở để lấy lastUpdated, so sánh với file local.
     */
    @GetMapping("/meta")
    public ResponseEntity<ApiResponse<ScheduleMetaDto>> getMeta(
            @RequestParam String studentCode) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getMeta(studentCode)));
    }

    /**
     * GET /api/v1/schedule/file?studentCode=2211020001
     *
     * Public — không cần token.
     * Trả về toàn bộ lịch dạng JSON. App lưu thành file local schedule_{studentCode}.json.
     * Chỉ gọi khi meta cho thấy server có version mới hơn file local.
     */
    @GetMapping("/file")
    public ResponseEntity<ApiResponse<ScheduleFileDto>> getFile(
            @RequestParam String studentCode) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getScheduleFile(studentCode)));
    }
}

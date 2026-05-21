package com.utc2.appreborn.backend.modules.dormitory.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.dormitory.dto.DormRegisterRequest;
import com.utc2.appreborn.backend.modules.dormitory.dto.DormRegistrationDto;
import com.utc2.appreborn.backend.modules.dormitory.dto.DormRoomDto;
import com.utc2.appreborn.backend.modules.dormitory.service.DormitoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dormitory")
@RequiredArgsConstructor
public class DormitoryController {

    private final DormitoryService dormitoryService;

    /**
     * GET /api/v1/dormitory/rooms
     * Danh sách tất cả phòng KTX — không cần token.
     */
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<DormRoomDto>>> getAllRooms() {
        return ResponseEntity.ok(ApiResponse.success(dormitoryService.getAllRooms()));
    }

    /**
     * GET /api/v1/dormitory/my
     * Lịch sử đăng ký KTX của sinh viên đang đăng nhập phải cần token.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<DormRegistrationDto>>> getMyRegistrations() {
        return ResponseEntity.ok(ApiResponse.success(dormitoryService.getMyRegistrations()));
    }

    /**
     * POST /api/v1/dormitory/register
     * Đăng ký phòng KTX — cần token.
     * Body: { "roomId": 1, "months": 8 }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<DormRegistrationDto>> register(
            @Valid @RequestBody DormRegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(dormitoryService.register(request)));
    }

    /**
     * DELETE /api/v1/dormitory/{dormRegId}
     * Hủy đăng ký KTX — cần token.
     */
    @DeleteMapping("/{dormRegId}")
    public ResponseEntity<ApiResponse<Void>> cancelRegistration(
            @PathVariable Long dormRegId) {
        dormitoryService.cancelRegistration(dormRegId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

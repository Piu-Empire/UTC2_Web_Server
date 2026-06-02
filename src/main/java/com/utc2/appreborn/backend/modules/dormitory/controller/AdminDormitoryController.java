package com.utc2.appreborn.backend.modules.dormitory.controller;

import com.utc2.appreborn.backend.common.constants.ApiConstant;
import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.dormitory.entity.DormitoryRoomEntity;
import com.utc2.appreborn.backend.modules.dormitory.entity.DormitoryRegistrationEntity;
import com.utc2.appreborn.backend.modules.dormitory.repository.DormitoryRoomRepository;
import com.utc2.appreborn.backend.modules.dormitory.repository.DormitoryRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstant.API_PREFIX)
@RequiredArgsConstructor
public class AdminDormitoryController {

    private final DormitoryRoomRepository roomRepository;
    private final DormitoryRegistrationRepository registrationRepository;

    // --- PHÒNG KÝ TÚC XÁ ---
    @GetMapping("/dormitories")
    public ResponseEntity<ApiResponse<List<DormitoryRoomEntity>>> getAllRooms() {
        List<DormitoryRoomEntity> rooms = roomRepository.findAll();
        return ResponseEntity.ok(ApiResponse.<List<DormitoryRoomEntity>>builder()
                .success(true)
                .data(rooms)
                .build());
    }

    @GetMapping("/dormitories/{id}")
    public ResponseEntity<ApiResponse<DormitoryRoomEntity>> getRoomById(@PathVariable Long id) {
        return roomRepository.findById(id)
                .map(room -> ResponseEntity.ok(ApiResponse.<DormitoryRoomEntity>builder()
                        .success(true)
                        .data(room)
                        .build()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- ĐĂNG KÝ KÝ TÚC XÁ ---
    @GetMapping("/dorm-registrations")
    public ResponseEntity<ApiResponse<List<DormitoryRegistrationEntity>>> getAllRegistrations() {
        List<DormitoryRegistrationEntity> registrations = registrationRepository.findAll();
        return ResponseEntity.ok(ApiResponse.<List<DormitoryRegistrationEntity>>builder()
                .success(true)
                .data(registrations)
                .build());
    }

    @GetMapping("/dorm-registrations/{id}")
    public ResponseEntity<ApiResponse<DormitoryRegistrationEntity>> getRegistrationById(@PathVariable Long id) {
        return registrationRepository.findById(id)
                .map(reg -> ResponseEntity.ok(ApiResponse.<DormitoryRegistrationEntity>builder()
                        .success(true)
                        .data(reg)
                        .build()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
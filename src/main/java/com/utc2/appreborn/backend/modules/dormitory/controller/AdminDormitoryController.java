package com.utc2.appreborn.backend.modules.dormitory.controller;

import com.utc2.appreborn.backend.common.constants.ApiConstant;
import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.dormitory.dto.DormRegistrationDto;
import com.utc2.appreborn.backend.modules.dormitory.entity.DormitoryRoomEntity;
import com.utc2.appreborn.backend.modules.dormitory.entity.DormitoryRegistrationEntity;
import com.utc2.appreborn.backend.modules.dormitory.repository.DormitoryRoomRepository;
import com.utc2.appreborn.backend.modules.dormitory.repository.DormitoryRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiConstant.API_PREFIX)
@RequiredArgsConstructor
public class AdminDormitoryController {

    private final DormitoryRoomRepository roomRepository;
    private final DormitoryRegistrationRepository registrationRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── PHÒNG KÝ TÚC XÁ ─────────────────────────────────────────────────────

    @GetMapping("/dormitories")
    public ResponseEntity<ApiResponse<List<DormitoryRoomEntity>>> getAllRooms() {
        return ResponseEntity.ok(ApiResponse.<List<DormitoryRoomEntity>>builder()
                .success(true).data(roomRepository.findAll()).build());
    }

    @GetMapping("/dormitories/{id}")
    public ResponseEntity<ApiResponse<DormitoryRoomEntity>> getRoomById(@PathVariable Long id) {
        return roomRepository.findById(id)
                .map(r -> ResponseEntity.ok(ApiResponse.<DormitoryRoomEntity>builder()
                        .success(true).data(r).build()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ── ĐĂNG KÝ KÝ TÚC XÁ (Admin) ──────────────────────────────────────────

    @GetMapping("/dorm-registrations")
    public ResponseEntity<ApiResponse<List<DormRegistrationDto>>> listRegistrations(
            @RequestParam(required = false) String status) {
        List<Object[]> rows = (status == null || status.isBlank())
                ? registrationRepository.findAllRegistrationsForAdmin()
                : registrationRepository.findAllRegistrationsForAdminByStatus(status);

        List<DormRegistrationDto> result = rows.stream().map(this::mapAdminRow).toList();
        return ResponseEntity.ok(ApiResponse.<List<DormRegistrationDto>>builder()
                .success(true).data(result).build());
    }

    @GetMapping("/dorm-registrations/{id}")
    public ResponseEntity<ApiResponse<DormitoryRegistrationEntity>> getRegistrationById(@PathVariable Long id) {
        return registrationRepository.findById(id)
                .map(r -> ResponseEntity.ok(ApiResponse.<DormitoryRegistrationEntity>builder()
                        .success(true).data(r).build()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/dorm-registrations/{id}/status")
    public ResponseEntity<ApiResponse<DormitoryRegistrationEntity>> updateRegistrationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return registrationRepository.findById(id)
                .map(reg -> {
                    reg.setStatus(body.get("status"));
                    registrationRepository.save(reg);
                    return ResponseEntity.ok(ApiResponse.<DormitoryRegistrationEntity>builder()
                            .success(true).data(reg).build());
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/dorm-registrations/{id}/approve")
    public ResponseEntity<ApiResponse<DormitoryRegistrationEntity>> approveRegistration(@PathVariable Long id) {
        return registrationRepository.findById(id)
                .map(reg -> {
                    reg.setStatus("đã duyệt");
                    registrationRepository.save(reg);
                    return ResponseEntity.ok(ApiResponse.<DormitoryRegistrationEntity>builder()
                            .success(true).message("Duyệt đăng ký thành công").data(reg).build());
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/dorm-registrations/{id}/reject")
    public ResponseEntity<ApiResponse<DormitoryRegistrationEntity>> rejectRegistration(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        return registrationRepository.findById(id)
                .map(reg -> {
                    reg.setStatus("từ chối");
                    registrationRepository.save(reg);
                    return ResponseEntity.ok(ApiResponse.<DormitoryRegistrationEntity>builder()
                            .success(true).message("Từ chối đăng ký thành công").data(reg).build());
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private DormRegistrationDto mapAdminRow(Object[] row) {
        return DormRegistrationDto.builder()
                .dormRegId(((Number) row[0]).longValue())
                .status((String) row[2])
                .totalFee(row[3] != null ? ((Number) row[3]).doubleValue() : null)
                .paidStatus((String) row[4])
                .startDate(formatDate(row[5]))
                .endDate(formatDate(row[6]))
                .registeredAt(row[7] != null ? row[7].toString() : null)
                .roomCode((String) row[8])
                .building((String) row[9])
                .roomType((String) row[10])
                .pricePerMonth(row[11] != null ? ((Number) row[11]).doubleValue() : null)
                .studentName((String) row[12])
                .studentCode((String) row[13])
                .email((String) row[14])
                .className((String) row[15])
                .build();
    }

    private String formatDate(Object o) {
        if (o == null) return null;
        try {
            if (o instanceof java.sql.Date d) return d.toLocalDate().format(DATE_FMT);
            return o.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
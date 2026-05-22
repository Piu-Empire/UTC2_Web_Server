package com.utc2.appreborn.backend.modules.public_services.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.public_services.dto.AdminUpdateStatusRequest;
import com.utc2.appreborn.backend.modules.public_services.dto.ServiceRequestResponse;
import com.utc2.appreborn.backend.modules.public_services.service.PublicServicesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/service-requests")
@RequiredArgsConstructor
@Tag(name = "Admin - Service Requests", description = "Quản lý yêu cầu dịch vụ công (Admin)")
@SecurityRequirement(name = "bearerAuth")
public class AdminServiceRequestController {

    private final PublicServicesService publicServicesService;

    /**
     * GET /api/v1/admin/service-requests
     * Lấy tất cả yêu cầu dịch vụ — có thể lọc theo status và serviceType
     */
    @GetMapping
    @Operation(summary = "Xem tất cả yêu cầu dịch vụ")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getAllRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String serviceType) {
        return ResponseEntity.ok(ApiResponse.success(
                publicServicesService.getAllRequests(status, serviceType)));
    }

    /**
     * PUT /api/v1/admin/service-requests/:id/status
     * Cập nhật trạng thái và ghi chú kết quả
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái yêu cầu")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                publicServicesService.updateRequestStatus(id, request)));
    }
}
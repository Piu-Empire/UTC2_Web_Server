package com.utc2.appreborn.backend.modules.public_services.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.public_services.dto.AdminUpdateStatusRequest;
import com.utc2.appreborn.backend.modules.public_services.dto.ServiceRequestResponse;
import com.utc2.appreborn.backend.modules.public_services.service.PublicServicesService;
import com.utc2.appreborn.backend.security.jwt.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final JwtService jwtService;

    /**
     * Kiểm tra caller có quyền truy cập service-requests không:
     * - ADMIN: luôn được
     * - STAFF lv5: được
     * - STAFF lv khác / các role khác: không được
     */
    private boolean hasServiceRequestAccess(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return false;
        String token = header.substring(7);
        List<String> roles = jwtService.getRolesFromToken(token);
        if (roles.contains("ROLE_ADMIN")) return true;
        if (roles.contains("ROLE_STAFF")) {
            Integer level = jwtService.getStaffLevelFromToken(token);
            return Integer.valueOf(5).equals(level);
        }
        return false;
    }

    /**
     * GET /api/v1/admin/service-requests
     * Lấy tất cả yêu cầu dịch vụ — có thể lọc theo status và serviceType
     */
    @GetMapping
    @Operation(summary = "Xem tất cả yêu cầu dịch vụ")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getAllRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String serviceType,
            HttpServletRequest request) {
        if (!hasServiceRequestAccess(request))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bạn không có quyền truy cập chức năng này"));
        return ResponseEntity.ok(ApiResponse.success(
                publicServicesService.getAllRequests(status, serviceType)));
    }

    /**
     * PUT /api/v1/admin/service-requests/:id/status
     * Cập nhật trạng thái và ghi chú kết quả
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái yêu cầu")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateStatusRequest req,
            HttpServletRequest request) {
        if (!hasServiceRequestAccess(request))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bạn không có quyền truy cập chức năng này"));
        return ResponseEntity.ok(ApiResponse.success(
                publicServicesService.updateRequestStatus(id, req)));
    }
}
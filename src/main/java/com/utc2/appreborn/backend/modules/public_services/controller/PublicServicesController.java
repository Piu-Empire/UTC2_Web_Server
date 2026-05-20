package com.utc2.appreborn.backend.modules.public_services.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.public_services.dto.*;
import com.utc2.appreborn.backend.modules.public_services.service.PublicServicesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Tag(name = "Public Services", description = "API dịch vụ công sinh viên")
@SecurityRequirement(name = "bearerAuth")
public class PublicServicesController {

    private final PublicServicesService publicServicesService;

    /** Xin cấp lại thẻ sinh viên */
    @PostMapping("/card-reissue")
    @Operation(summary = "Nộp yêu cầu cấp lại thẻ SV")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> cardReissue(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CardReissueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                publicServicesService.submitCardReissue(userDetails.getUsername(), request)));
    }

    /** Xin hỗ trợ vay vốn */
    @PostMapping("/loan-support")
    @Operation(summary = "Nộp yêu cầu hỗ trợ vay vốn")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> loanSupport(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody LoanSupportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                publicServicesService.submitLoanSupport(userDetails.getUsername(), request)));
    }

    /** Xin bảng điểm */
    @PostMapping("/transcript")
    @Operation(summary = "Nộp yêu cầu cấp bảng điểm")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> transcript(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TranscriptRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                publicServicesService.submitTranscript(userDetails.getUsername(), request)));
    }

    /** Xin giấy xác nhận sinh viên */
    @PostMapping("/student-confirmation")
    @Operation(summary = "Nộp yêu cầu giấy xác nhận sinh viên")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> studentConfirmation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StudentConfirmationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                publicServicesService.submitStudentConfirmation(userDetails.getUsername(), request)));
    }

    /** Lịch sử tất cả yêu cầu của mình */
    @GetMapping("/my-requests")
    @Operation(summary = "Lịch sử tất cả yêu cầu dịch vụ")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> myRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                publicServicesService.getMyRequests(userDetails.getUsername())));
    }

    /**
     * Lịch sử theo loại dịch vụ
     * type: TRANSCRIPT | CONFIRMATION_LETTER | CARD_REISSUE | LOAN_SUPPORT
     */
    @GetMapping("/my-requests/{type}")
    @Operation(summary = "Lịch sử yêu cầu theo loại dịch vụ")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> myRequestsByType(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String type) {
        return ResponseEntity.ok(ApiResponse.success(
                publicServicesService.getMyRequestsByType(userDetails.getUsername(), type)));
    }
}
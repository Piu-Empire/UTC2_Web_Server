package com.utc2.appreborn.backend.modules.public_services.service;

import com.utc2.appreborn.backend.modules.public_services.dto.*;

import java.util.List;

public interface PublicServicesService {

    // ── Student endpoints ────────────────────────────────────
    ServiceRequestResponse submitCardReissue(String username, CardReissueRequest request);
    ServiceRequestResponse submitLoanSupport(String username, LoanSupportRequest request);
    ServiceRequestResponse submitTranscript(String username, TranscriptRequest request);
    ServiceRequestResponse submitStudentConfirmation(String username, StudentConfirmationRequest request);
    List<ServiceRequestResponse> getMyRequests(String username);
    List<ServiceRequestResponse> getMyRequestsByType(String username, String serviceType);

    // ── Admin endpoints ──────────────────────────────────────
    /**
     * Lấy tất cả yêu cầu, tuỳ chọn lọc theo status và serviceType.
     * status: PENDING | PROCESSING | COMPLETED | REJECTED (null = không lọc)
     * serviceType: TRANSCRIPT | CONFIRMATION_LETTER | CARD_REISSUE | LOAN_SUPPORT (null = không lọc)
     */
    List<ServiceRequestResponse> getAllRequests(String status, String serviceType);

    /**
     * Cập nhật trạng thái và ghi chú kết quả của một yêu cầu.
     */
    ServiceRequestResponse updateRequestStatus(Long id, AdminUpdateStatusRequest request);
}
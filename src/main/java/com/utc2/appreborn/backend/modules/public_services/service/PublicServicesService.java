package com.utc2.appreborn.backend.modules.public_services.service;

import com.utc2.appreborn.backend.modules.public_services.dto.*;

import java.util.List;

public interface PublicServicesService {
    ServiceRequestResponse submitCardReissue(String username, CardReissueRequest request);
    ServiceRequestResponse submitLoanSupport(String username, LoanSupportRequest request);
    ServiceRequestResponse submitTranscript(String username, TranscriptRequest request);
    ServiceRequestResponse submitStudentConfirmation(String username, StudentConfirmationRequest request);
    List<ServiceRequestResponse> getMyRequests(String username);
    List<ServiceRequestResponse> getMyRequestsByType(String username, String serviceType);
}
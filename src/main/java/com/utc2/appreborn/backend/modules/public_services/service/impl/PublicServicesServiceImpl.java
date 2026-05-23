package com.utc2.appreborn.backend.modules.public_services.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.auth.entity.User;
import com.utc2.appreborn.backend.modules.public_services.dto.*;
import com.utc2.appreborn.backend.modules.public_services.entity.ServiceRequest;
import com.utc2.appreborn.backend.modules.public_services.repository.ServiceRequestRepository;
import com.utc2.appreborn.backend.modules.public_services.service.PublicServicesService;
import com.utc2.appreborn.backend.modules.profile.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicServicesServiceImpl implements PublicServicesService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ObjectMapper             objectMapper;

    // ── Student methods ──────────────────────────────────────

    @Override
    public ServiceRequestResponse submitCardReissue(String username, CardReissueRequest req) {
        User user = findUser(username);
        String desc = req.getReason() != null ? req.getReason() : "Không có lý do cụ thể";
        return save(user, "CARD_REISSUE", desc);
    }

    @Override
    public ServiceRequestResponse submitLoanSupport(String username, LoanSupportRequest req) {
        User user = findUser(username);
        String desc = toJson(Map.of(
                "loan_amount",  req.getLoanAmount(),
                "loan_reason",  req.getLoanReason(),
                "phone_number", req.getPhoneNumber()
        ));
        return save(user, "LOAN_SUPPORT", desc);
    }

    @Override
    public ServiceRequestResponse submitTranscript(String username, TranscriptRequest req) {
        User user = findUser(username);
        String desc = toJson(Map.of(
                "academic_year", req.getAcademicYear(),
                "semester",      req.getSemester(),
                "quantity",      req.getQuantity(),
                "note",          req.getNote() != null ? req.getNote() : ""
        ));
        return save(user, "TRANSCRIPT", desc);
    }

    @Override
    public ServiceRequestResponse submitStudentConfirmation(String username, StudentConfirmationRequest req) {
        User user = findUser(username);
        String desc = toJson(Map.of(
                "purpose",  req.getPurpose(),
                "quantity", req.getQuantity()
        ));
        return save(user, "CONFIRMATION_LETTER", desc);
    }

    @Override
    public List<ServiceRequestResponse> getMyRequests(String username) {
        User user = findUser(username);
        return serviceRequestRepository
                .findByUserIdOrderBySubmittedAtDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ServiceRequestResponse> getMyRequestsByType(String username, String serviceType) {
        User user = findUser(username);
        return serviceRequestRepository
                .findByUserIdAndServiceTypeOrderBySubmittedAtDesc(user.getId(), serviceType)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Admin methods ────────────────────────────────────────

    @Override
    public List<ServiceRequestResponse> getAllRequests(String status, String serviceType) {
        List<ServiceRequest> results;

        if (status != null && serviceType != null) {
            results = serviceRequestRepository
                    .findByStatusAndServiceTypeOrderBySubmittedAtDesc(status, serviceType);
        } else if (status != null) {
            results = serviceRequestRepository
                    .findByStatusOrderBySubmittedAtDesc(status);
        } else if (serviceType != null) {
            results = serviceRequestRepository
                    .findByServiceTypeOrderBySubmittedAtDesc(serviceType);
        } else {
            results = serviceRequestRepository
                    .findAllByOrderBySubmittedAtDesc();
        }

        return results.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public ServiceRequestResponse updateRequestStatus(Long id, AdminUpdateStatusRequest request) {
        ServiceRequest req = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy yêu cầu dịch vụ với id: " + id));

        req.setStatus(request.getStatus());
        if (request.getResultNote() != null) {
            req.setResultNote(request.getResultNote());
        }
        // Nếu COMPLETED hoặc REJECTED thì set resolvedAt
        if ("COMPLETED".equals(request.getStatus()) || "REJECTED".equals(request.getStatus())) {
            req.setResolvedAt(LocalDateTime.now());
        }

        return toResponse(serviceRequestRepository.save(req));
    }

    // ── Helpers ──────────────────────────────────────────────

    private ServiceRequestResponse save(User user, String type, String description) {
        ServiceRequest req = ServiceRequest.builder()
                .user(user)
                .serviceType(type)
                .description(description)
                .status("PENDING")
                .build();
        return toResponse(serviceRequestRepository.save(req));
    }

    private User findUser(String username) {
        String studentCode = username.contains("@") ? username.split("@")[0] : username;
        return studentProfileRepository.findByStudentCodeWithUser(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sinh viên: " + studentCode))
                .getUser();
    }

    private String toJson(Map<String, Object> map) {
        try { return objectMapper.writeValueAsString(map); }
        catch (JsonProcessingException e) { return "{}"; }
    }

    private ServiceRequestResponse toResponse(ServiceRequest r) {
        return ServiceRequestResponse.builder()
                .id(r.getId())
                .serviceType(r.getServiceType())
                .description(r.getDescription())
                .status(r.getStatus())
                .resultNote(r.getResultNote())
                .submittedAt(r.getSubmittedAt())
                .resolvedAt(r.getResolvedAt())
                .build();
    }
}
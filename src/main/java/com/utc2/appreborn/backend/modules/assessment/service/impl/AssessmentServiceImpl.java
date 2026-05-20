package com.utc2.appreborn.backend.modules.assessment.service.impl;

import com.utc2.appreborn.backend.modules.assessment.dto.*;
import com.utc2.appreborn.backend.modules.assessment.entity.*;
import com.utc2.appreborn.backend.modules.assessment.repository.*;
import com.utc2.appreborn.backend.modules.assessment.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentPeriodRepository   periodRepo;
    private final StudentAssessmentRepository  studentRepo;
    private final AdvisorAssessmentRepository  advisorRepo;
    private final ExternalAssessmentRepository externalRepo;

    // ─── Học kỳ ───────────────────────────────────────────────────────────────

    @Override
    public List<AssessmentPeriod> getPeriods() {
        return periodRepo.findAllByOrderByPeriodIdDesc();
    }

    // ─── Sinh viên tự đánh giá ────────────────────────────────────────────────

    @Override
    @Transactional
    public void saveStudentAssessment(Long userId, SaveStudentAssessmentRequest request) {
        // Xóa dữ liệu cũ rồi insert lại (upsert đơn giản theo batch)
        studentRepo.deleteByUserIdAndPeriodId(userId, request.getPeriodId());

        List<StudentAssessment> entities = new ArrayList<>();
        for (SaveStudentAssessmentRequest.CriteriaScoreItem item : request.getItems()) {
            String evidenceUris = null;
            if (item.getEvidenceUris() != null && !item.getEvidenceUris().isEmpty()) {
                evidenceUris = String.join("|", item.getEvidenceUris());
            }
            entities.add(StudentAssessment.builder()
                    .userId(userId)
                    .periodId(request.getPeriodId())
                    .criteriaId(item.getCriteriaId())
                    .score(item.getScore() != null ? item.getScore() : BigDecimal.ZERO)
                    .evidenceUris(evidenceUris)
                    .build());
        }
        studentRepo.saveAll(entities);
    }

    @Override
    public StudentAssessmentResponse getStudentAssessment(Long userId, String periodId) {
        List<StudentAssessment> rows = studentRepo.findByUserIdAndPeriodId(userId, periodId);
        return buildStudentResponse(userId, periodId, rows);
    }

    // ─── Đánh giá CVHT ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void saveAdvisorAssessment(Long userId, SaveAdvisorAssessmentRequest request) {
        advisorRepo.deleteByUserIdAndPeriodId(userId, request.getPeriodId());

        List<AdvisorAssessment> entities = new ArrayList<>();
        for (SaveAdvisorAssessmentRequest.CriteriaScoreItem item : request.getItems()) {
            entities.add(AdvisorAssessment.builder()
                    .userId(userId)
                    .periodId(request.getPeriodId())
                    .criteriaId(item.getCriteriaId())
                    .score(item.getScore() != null ? item.getScore() : BigDecimal.ZERO)
                    .studentOpinion(request.getStudentOpinion())
                    .build());
        }
        advisorRepo.saveAll(entities);
    }

    // ─── External (Admin import) ──────────────────────────────────────────────

    @Override
    @Transactional
    public void importExternalAssessment(ImportExternalAssessmentRequest request) {
        externalRepo.deleteByUserIdAndPeriodId(request.getUserId(), request.getPeriodId());

        List<ExternalAssessment> entities = new ArrayList<>();
        for (ImportExternalAssessmentRequest.ExternalScoreItem item : request.getItems()) {
            entities.add(ExternalAssessment.builder()
                    .userId(request.getUserId())
                    .periodId(request.getPeriodId())
                    .criteriaId(item.getCriteriaId())
                    .tapTheScore(nvl(item.getTapTheScore()))
                    .khoaScore(nvl(item.getKhoaScore()))
                    .truongScore(nvl(item.getTruongScore()))
                    .build());
        }
        externalRepo.saveAll(entities);
    }

    @Override
    public ExternalAssessmentResponse getExternalAssessment(Long userId, String periodId) {
        List<ExternalAssessment> rows = externalRepo.findByUserIdAndPeriodId(userId, periodId);
        List<ExternalAssessmentResponse.ExternalScoreDto> dtos = rows.stream()
                .map(r -> ExternalAssessmentResponse.ExternalScoreDto.builder()
                        .criteriaId(r.getCriteriaId())
                        .tapTheScore(r.getTapTheScore())
                        .khoaScore(r.getKhoaScore())
                        .truongScore(r.getTruongScore())
                        .build())
                .collect(Collectors.toList());

        return ExternalAssessmentResponse.builder()
                .userId(userId)
                .periodId(periodId)
                .items(dtos)
                .build();
    }

    // ─── Admin: xem toàn bộ ──────────────────────────────────────────────────

    @Override
    public List<StudentAssessmentResponse> getAllStudentAssessments(String periodId) {
        List<StudentAssessment> all = studentRepo.findByPeriodId(periodId);
        // Gom nhóm theo userId
        Map<Long, List<StudentAssessment>> byUser = all.stream()
                .collect(Collectors.groupingBy(StudentAssessment::getUserId));

        return byUser.entrySet().stream()
                .map(e -> buildStudentResponse(e.getKey(), periodId, e.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentAssessmentResponse> getAllAdvisorAssessments(String periodId) {
        List<AdvisorAssessment> all = advisorRepo.findByPeriodId(periodId);
        Map<Long, List<AdvisorAssessment>> byUser = all.stream()
                .collect(Collectors.groupingBy(AdvisorAssessment::getUserId));

        return byUser.entrySet().stream()
                .map(e -> buildAdvisorResponse(e.getKey(), periodId, e.getValue()))
                .collect(Collectors.toList());
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private StudentAssessmentResponse buildStudentResponse(
            Long userId, String periodId, List<StudentAssessment> rows) {

        List<StudentAssessmentResponse.CriteriaScoreDto> dtos = rows.stream()
                .map(r -> StudentAssessmentResponse.CriteriaScoreDto.builder()
                        .criteriaId(r.getCriteriaId())
                        .score(r.getScore())
                        .evidenceUris(parseUris(r.getEvidenceUris()))
                        .build())
                .collect(Collectors.toList());

        return StudentAssessmentResponse.builder()
                .userId(userId)
                .periodId(periodId)
                .items(dtos)
                .submittedAt(rows.isEmpty() ? null : rows.get(0).getSubmittedAt())
                .build();
    }

    private StudentAssessmentResponse buildAdvisorResponse(
            Long userId, String periodId, List<AdvisorAssessment> rows) {

        List<StudentAssessmentResponse.CriteriaScoreDto> dtos = rows.stream()
                .map(r -> StudentAssessmentResponse.CriteriaScoreDto.builder()
                        .criteriaId(r.getCriteriaId())
                        .score(r.getScore())
                        .evidenceUris(Collections.emptyList())
                        .build())
                .collect(Collectors.toList());

        return StudentAssessmentResponse.builder()
                .userId(userId)
                .periodId(periodId)
                .items(dtos)
                .submittedAt(rows.isEmpty() ? null : rows.get(0).getSubmittedAt())
                .build();
    }

    private List<String> parseUris(String raw) {
        if (raw == null || raw.isBlank()) return Collections.emptyList();
        return Arrays.asList(raw.split("\\|"));
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
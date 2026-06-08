package com.utc2.appreborn.backend.modules.assessment.service.impl;

import com.utc2.appreborn.backend.modules.assessment.dto.*;
import com.utc2.appreborn.backend.modules.assessment.entity.*;
import com.utc2.appreborn.backend.modules.assessment.repository.*;
import com.utc2.appreborn.backend.modules.assessment.service.AssessmentService;
import com.utc2.appreborn.backend.modules.profile.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentPeriodRepository        periodRepo;
    private final StudentAssessmentRepository       studentRepo;
    private final AdvisorAssessmentRepository       advisorRepo;
    private final ExternalAssessmentRepository      externalRepo;
    private final ExternalAssessmentStatusRepository statusRepo;
    private final StudentProfileRepository          studentProfileRepo;

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

    @Override
    public AdvisorAssessmentResponse getAdvisorAssessment(Long userId, String periodId) {
        List<AdvisorAssessment> rows = advisorRepo.findByUserIdAndPeriodId(userId, periodId);
        return buildAdvisorResponse(userId, periodId, rows);
    }

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
                    .boMonScore(nvl(item.getBoMonScore()))
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
                        .boMonScore(r.getBoMonScore())
                        .khoaScore(r.getKhoaScore())
                        .truongScore(r.getTruongScore())
                        .build())
                .collect(Collectors.toList());

        ExternalAssessmentStatus status = statusRepo
                .findById(new ExternalAssessmentStatusId(userId, periodId))
                .orElse(null);

        return ExternalAssessmentResponse.builder()
                .userId(userId)
                .periodId(periodId)
                .items(dtos)
                .advisorApproved(status != null && status.isAdvisorApproved())
                .khoaApproved(status != null && status.isKhoaApproved())
                .truongApproved(status != null && status.isTruongApproved())
                .build();
    }

    @Override
    @Transactional
    public void setTapTheScore(SetExternalScoreRequest request) {
        upsertExternalScore(request, "tapThe");
    }

    @Override
    @Transactional
    public void setBoMonScore(SetExternalScoreRequest request) {
        upsertExternalScore(request, "boMon");
    }

    @Override
    @Transactional
    public void setKhoaScore(SetExternalScoreRequest request) {
        upsertExternalScore(request, "khoa");
    }

    @Override
    @Transactional
    public void setTruongScore(SetExternalScoreRequest request) {
        upsertExternalScore(request, "truong");
    }

    @Override
    @Transactional
    public void approveAdvisor(ApproveRequest request) {
        ExternalAssessmentStatus status = getOrCreateStatus(request.getUserId(), request.getPeriodId());
        status.setAdvisorApproved(true);
        status.setAdvisorApprovedAt(java.time.LocalDateTime.now());
        statusRepo.save(status);
    }

    @Override
    @Transactional
    public void approveKhoa(ApproveRequest request) {
        ExternalAssessmentStatus status = getOrCreateStatus(request.getUserId(), request.getPeriodId());
        status.setKhoaApproved(true);
        status.setKhoaApprovedAt(java.time.LocalDateTime.now());
        statusRepo.save(status);
    }

    @Override
    @Transactional
    public void approveTruong(ApproveRequest request) {
        ExternalAssessmentStatus status = getOrCreateStatus(request.getUserId(), request.getPeriodId());
        status.setTruongApproved(true);
        status.setTruongApprovedAt(java.time.LocalDateTime.now());
        statusRepo.save(status);
    }

    @Override
    public List<StudentOverviewResponse> getStudentOverview(String periodId) {
        List<StudentAssessment> allStudent = studentRepo.findByPeriodId(periodId);
        Map<Long, List<StudentAssessment>> byUser = allStudent.stream()
                .collect(Collectors.groupingBy(StudentAssessment::getUserId));

        List<ExternalAssessment> allExternal = externalRepo.findByPeriodId(periodId);
        Map<Long, List<ExternalAssessment>> exByUser = allExternal.stream()
                .collect(Collectors.groupingBy(ExternalAssessment::getUserId));

        List<ExternalAssessmentStatus> allStatus = statusRepo.findByPeriodId(periodId);
        Map<Long, ExternalAssessmentStatus> statusByUser = allStatus.stream()
                .collect(Collectors.toMap(ExternalAssessmentStatus::getUserId, s -> s));

        return byUser.entrySet().stream().map(e -> {
            Long userId = e.getKey();
            List<StudentAssessment> svRows = e.getValue();
            List<ExternalAssessment> exRows = exByUser.getOrDefault(userId, Collections.emptyList());
            ExternalAssessmentStatus status = statusByUser.get(userId);

            // Tổng điểm SV tự đánh giá
            BigDecimal svTotal = svRows.stream()
                    .map(StudentAssessment::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Tổng từng cột external
            BigDecimal tapThe = sumColumn(exRows, ExternalAssessment::getTapTheScore);
            BigDecimal boMon  = sumColumn(exRows, ExternalAssessment::getBoMonScore);
            BigDecimal khoa   = sumColumn(exRows, ExternalAssessment::getKhoaScore);
            BigDecimal truong = sumColumn(exRows, ExternalAssessment::getTruongScore);

            // Map external theo criteriaId để join
            Map<Integer, ExternalAssessment> exByCriteria = exRows.stream()
                    .collect(Collectors.toMap(ExternalAssessment::getCriteriaId, x -> x, (a, b) -> a));

            List<StudentOverviewResponse.CriteriaDetail> details = svRows.stream()
                    .map(sv -> {
                        ExternalAssessment ex = exByCriteria.get(sv.getCriteriaId());
                        return StudentOverviewResponse.CriteriaDetail.builder()
                                .criteriaId(sv.getCriteriaId())
                                .studentScore(sv.getScore())
                                .evidenceUris(parseUris(sv.getEvidenceUris()))
                                .tapTheScore(ex != null ? ex.getTapTheScore() : BigDecimal.ZERO)
                                .boMonScore(ex != null ? ex.getBoMonScore() : BigDecimal.ZERO)
                                .khoaScore(ex != null ? ex.getKhoaScore() : BigDecimal.ZERO)
                                .truongScore(ex != null ? ex.getTruongScore() : BigDecimal.ZERO)
                                .build();
                    }).collect(Collectors.toList());

            return StudentOverviewResponse.builder()
                    .userId(userId)
                    .studentCode(studentProfileRepo.findById(userId)
                            .map(p -> p.getStudentCode()).orElse(""))
                    .periodId(periodId)
                    .studentTotalScore(svTotal)
                    .tapTheScore(tapThe)
                    .boMonScore(boMon)
                    .khoaScore(khoa)
                    .truongScore(truong)
                    .advisorApproved(status != null && status.isAdvisorApproved())
                    .khoaApproved(status != null && status.isKhoaApproved())
                    .truongApproved(status != null && status.isTruongApproved())
                    .advisorApprovedAt(status != null ? status.getAdvisorApprovedAt() : null)
                    .khoaApprovedAt(status != null ? status.getKhoaApprovedAt() : null)
                    .truongApprovedAt(status != null ? status.getTruongApprovedAt() : null)
                    .submittedAt(svRows.isEmpty() ? null : svRows.get(0).getSubmittedAt())
                    .criteriaDetails(details)
                    .build();
        }).collect(Collectors.toList());
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
    public List<AdvisorAssessmentResponse> getAllAdvisorAssessments(String periodId) {
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

    private AdvisorAssessmentResponse buildAdvisorResponse(
            Long userId, String periodId, List<AdvisorAssessment> rows) {

        List<AdvisorAssessmentResponse.CriteriaScoreDto> dtos = rows.stream()
                .map(r -> AdvisorAssessmentResponse.CriteriaScoreDto.builder()
                        .criteriaId(r.getCriteriaId())
                        .score(r.getScore())
                        .build())
                .collect(Collectors.toList());

        // studentOpinion là giống nhau cho tất cả row cùng (userId, periodId)
        String opinion = rows.stream()
                .map(AdvisorAssessment::getStudentOpinion)
                .filter(o -> o != null && !o.isBlank())
                .findFirst()
                .orElse(null);

        return AdvisorAssessmentResponse.builder()
                .userId(userId)
                .periodId(periodId)
                .studentOpinion(opinion)
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

    private BigDecimal sumColumn(List<ExternalAssessment> rows, Function<ExternalAssessment, BigDecimal> getter) {
        return rows.stream().map(getter).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ExternalAssessmentStatus getOrCreateStatus(Long userId, String periodId) {
        return statusRepo.findById(new ExternalAssessmentStatusId(userId, periodId))
                .orElse(ExternalAssessmentStatus.builder()
                        .userId(userId)
                        .periodId(periodId)
                        .advisorApproved(false)
                        .khoaApproved(false)
                        .truongApproved(false)
                        .build());
    }

    private void upsertExternalScore(SetExternalScoreRequest request, String column) {
        for (SetExternalScoreRequest.CriteriaScore item : request.getItems()) {
            ExternalAssessment row = externalRepo
                    .findByUserIdAndPeriodIdAndCriteriaId(
                            request.getUserId(), request.getPeriodId(), item.getCriteriaId())
                    .orElse(ExternalAssessment.builder()
                            .userId(request.getUserId())
                            .periodId(request.getPeriodId())
                            .criteriaId(item.getCriteriaId())
                            .tapTheScore(BigDecimal.ZERO)
                            .boMonScore(BigDecimal.ZERO)
                            .khoaScore(BigDecimal.ZERO)
                            .truongScore(BigDecimal.ZERO)
                            .build());
            BigDecimal score = nvl(item.getScore());
            switch (column) {
                case "tapThe" -> row.setTapTheScore(score);
                case "boMon"  -> row.setBoMonScore(score);
                case "khoa"   -> row.setKhoaScore(score);
                case "truong" -> row.setTruongScore(score);
            }
            externalRepo.save(row);
        }
    }
}
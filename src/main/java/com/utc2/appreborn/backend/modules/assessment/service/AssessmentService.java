package com.utc2.appreborn.backend.modules.assessment.service;

import com.utc2.appreborn.backend.modules.assessment.dto.*;
import com.utc2.appreborn.backend.modules.assessment.entity.AssessmentPeriod;

import java.util.List;

public interface AssessmentService {

    // ─── Học kỳ ───────────────────────────────────────────────────────────────

    List<AssessmentPeriod> getPeriods();

    // ─── Sinh viên tự đánh giá (App → Server) ────────────────────────────────

    void saveStudentAssessment(Long userId, SaveStudentAssessmentRequest request);

    StudentAssessmentResponse getStudentAssessment(Long userId, String periodId);

    // ─── Đánh giá CVHT (App → Server) ────────────────────────────────────────

    void saveAdvisorAssessment(Long userId, SaveAdvisorAssessmentRequest request);

    AdvisorAssessmentResponse getAdvisorAssessment(Long userId, String periodId);

    // ─── Điểm external (Admin → Server → App) ────────────────────────────────

    /** Admin import điểm Khoa/Lớp/Trường cho một sinh viên */
    void importExternalAssessment(ImportExternalAssessmentRequest request);

    /** App lấy điểm readonly Tập thể lớp / Khoa/BM / Trường */
    ExternalAssessmentResponse getExternalAssessment(Long userId, String periodId);

    /** Staff lv1 nhập tap_the_score */
    void setTapTheScore(SetExternalScoreRequest request);

    /** Staff lv3 nhập bo_mon_score */
    void setBoMonScore(SetExternalScoreRequest request);

    /** Staff lv4 nhập khoa_score */
    void setKhoaScore(SetExternalScoreRequest request);

    /** Staff lv5 / Admin nhập truong_score */
    void setTruongScore(SetExternalScoreRequest request);

    /** Advisor duyệt */
    void approveAdvisor(ApproveRequest request);

    /** Khoa duyệt */
    void approveKhoa(ApproveRequest request);

    /** Trường duyệt */
    void approveTruong(ApproveRequest request);

    // ─── Admin xem dữ liệu ────────────────────────────────────────────────────

    /** Admin lấy toàn bộ đánh giá SV trong 1 học kỳ */
    List<StudentAssessmentResponse> getAllStudentAssessments(String periodId);

    /** Admin lấy toàn bộ đánh giá CVHT trong 1 học kỳ */
    List<AdvisorAssessmentResponse> getAllAdvisorAssessments(String periodId);

    /** Admin/Staff xem overview tất cả SV (gồm external scores + approval) */
    List<StudentOverviewResponse> getStudentOverview(String periodId);
}
package com.utc2.appreborn.backend.modules.academic.service;

import com.utc2.appreborn.backend.modules.academic.dto.*;

import java.util.List;

public interface AcademicService {

    // ── Student / App ──────────────────────────────────────────────────────
    List<SemesterDto>         getSemesters(Long userId);
    List<CourseGradeDto>      getGrades(Long userId, Long semesterId);
    List<LeaderboardEntryDto> getLeaderboard(Long semesterId, String academicYear);
    List<ScholarshipDto>      getScholarships(Long userId);
    List<AcademicWarningDto>  getWarnings(Long userId, Long semesterId);

    // ── STAFF lv2 (Giảng viên) ────────────────────────────────────────────
    /** Xem danh sách sinh viên theo môn + lớp để nhập điểm */
    List<GradesByCourseDto>   getGradesByCourse(Long courseId, String className);
    /** Nhập điểm 1 sinh viên — chỉ STAFF lv2 và đúng môn mình dạy */
    CourseGradeDto            updateGrade(Long enrollmentId, GradeUpdateDto dto);

    // ── ADVISOR (Cố vấn học tập) ──────────────────────────────────────────
    AcademicWarningDto        upsertWarning(WarningUpsertDto dto);
    void                      deleteWarning(Long warningId);
    ScholarshipDto            updateScholarshipStatus(ScholarshipUpsertDto dto);
}
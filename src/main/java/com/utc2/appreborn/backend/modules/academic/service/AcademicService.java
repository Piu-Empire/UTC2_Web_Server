package com.utc2.appreborn.backend.modules.academic.service;

import com.utc2.appreborn.backend.modules.academic.dto.*;
import java.util.List;

public interface AcademicService {

    // ── Student / App ─────────────────────────────────────────────────────
    List<SemesterDto>         getSemesters(Long userId);
    List<CourseGradeDto>      getGrades(Long userId, Long semesterId);
    List<LeaderboardEntryDto> getLeaderboard(Long semesterId, String academicYear, String className);
    List<ScholarshipDto>      getScholarships(Long userId);
    List<AcademicWarningDto>  getWarnings(Long userId, Long semesterId);

    // ── STAFF lv2 (Giảng viên) ────────────────────────────────────────────
    List<GradesByCourseDto>   getGradesByCourse(Long courseId, String className);
    CourseGradeDto            updateGrade(Long enrollmentId, GradeUpdateDto dto);

    // ── ADVISOR + lv5: import warning & scholarship ───────────────────────
    AcademicWarningDto        upsertWarning(WarningUpsertDto dto);
    void                      deleteWarning(Long warningId);
    ScholarshipDto            updateScholarshipStatus(ScholarshipUpsertDto dto);

    // ── lv5 + ADMIN: duyệt ───────────────────────────────────────────────
    void                      approveLeaderboard(Long semesterId);
    void                      revokeLeaderboard(Long semesterId);
    List<LeaderboardEntryDto> getPendingLeaderboard(Long semesterId, String academicYear);

    AcademicWarningDto        approveWarning(Long warningId);
    List<AcademicWarningDto>  getPendingWarnings();

    ScholarshipDto            approveScholarship(Long userId, Long scholarshipId);
    ScholarshipDto            markScholarshipReceived(Long userId, Long scholarshipId);
    List<Object>              getPendingScholarships();

    // ── Teacher course ────────────────────────────────────────────────────
    List<TeacherCourseDto>    getMyTeacherCourses();
    TeacherCourseDto          assignTeacher(Long userId, Long courseId, Long semesterId, String className);
    void                      removeTeacherCourse(Long id);
}
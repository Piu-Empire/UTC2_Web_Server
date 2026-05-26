package com.utc2.appreborn.backend.modules.academic.service.impl;

import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.academic.dto.*;
import com.utc2.appreborn.backend.modules.academic.entity.AcademicWarningEntity;
import com.utc2.appreborn.backend.modules.academic.entity.SemesterEntity;
import com.utc2.appreborn.backend.modules.academic.repository.AcademicWarningRepository;
import com.utc2.appreborn.backend.modules.academic.repository.ScholarshipRepository;
import com.utc2.appreborn.backend.modules.academic.repository.SemesterRepository;
import com.utc2.appreborn.backend.modules.academic.service.AcademicService;
import com.utc2.appreborn.backend.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class AcademicServiceImpl implements AcademicService {

    private final SemesterRepository        semesterRepository;
    private final AcademicWarningRepository warningRepository;
    private final ScholarshipRepository     scholarshipRepository;
    private final UserRepository            userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // ── Helper ────────────────────────────────────────────────────────────────

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  1. SEMESTERS
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public List<SemesterDto> getSemesters() {
        Long userId = currentUserId();
        List<SemesterEntity> list = semesterRepository.findByUser_IdOrderBySemesterNumberAsc(userId);
        List<SemesterDto> result = new ArrayList<>();
        for (SemesterEntity s : list) {
            result.add(SemesterDto.builder()
                    .semesterId(s.getSemesterId())
                    .semesterName(s.getSemesterName())
                    .academicYear(s.getAcademicYear())
                    .semesterNumber(s.getSemesterNumber())
                    .startDate(s.getStartDate() != null ? s.getStartDate().toString() : null)
                    .endDate(s.getEndDate() != null ? s.getEndDate().toString() : null)
                    .gpa(s.getGpa() != null ? s.getGpa().doubleValue() : null)
                    .totalCredits(s.getTotalCredits())
                    .passedCredits(s.getPassedCredits())
                    .build());
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  2. GRADES
    //  semesterId == null  → tất cả các kỳ
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public List<CourseGradeDto> getGrades(Long semesterId) {
        Long userId = currentUserId();

        String sql = """
                SELECT e.enrollment_id, c.course_code, c.course_name, c.credits,
                       e.midterm_score, e.final_score, e.assignment_score, e.total_score,
                       e.letter_grade, e.grade_point, e.is_passed, e.status,
                       s.semester_id, s.semester_name, s.academic_year
                FROM enrollment e
                JOIN course c ON c.course_id = e.course_id
                JOIN semester s ON s.semester_id = e.semester_id
                WHERE e.user_id = :userId
                """ +
                (semesterId != null ? " AND e.semester_id = :semesterId" : "") +
                " ORDER BY s.semester_number ASC, c.course_code ASC";

        var query = entityManager.createNativeQuery(sql)
                .setParameter("userId", userId);
        if (semesterId != null) {
            query.setParameter("semesterId", semesterId);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<CourseGradeDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(CourseGradeDto.builder()
                    .enrollmentId(((Number) row[0]).longValue())
                    .courseCode((String) row[1])
                    .courseName((String) row[2])
                    .credits(row[3] != null ? ((Number) row[3]).intValue() : null)
                    .midtermScore(row[4] != null ? ((Number) row[4]).doubleValue() : null)
                    .finalScore(row[5] != null ? ((Number) row[5]).doubleValue() : null)
                    .assignmentScore(row[6] != null ? ((Number) row[6]).doubleValue() : null)
                    .totalScore(row[7] != null ? ((Number) row[7]).doubleValue() : null)
                    .letterGrade((String) row[8])
                    .gradePoint(row[9] != null ? ((Number) row[9]).doubleValue() : null)
                    .isPassed(row[10] != null ? (Boolean) row[10] : null)
                    .status((String) row[11])
                    .semesterId(row[12] != null ? ((Number) row[12]).longValue() : null)
                    .semesterName((String) row[13])
                    .academicYear((String) row[14])
                    .build());
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  3. LEADERBOARD
    //  Xếp hạng GPA tích lũy trong cùng academicYear hoặc semesterId.
    //  Dùng bảng semester (đã có gpa, total_credits sẵn).
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public List<LeaderboardEntryDto> getLeaderboard(Long semesterId, String academicYear) {
        Long currentUserId = currentUserId();

        // Xác định academicYear để scope leaderboard
        String targetYear = academicYear;
        if (targetYear == null && semesterId != null) {
            targetYear = semesterRepository.findById(semesterId)
                    .map(SemesterEntity::getAcademicYear)
                    .orElse(null);
        }

        String sql;
        if (semesterId != null) {
            // Xếp hạng theo 1 kỳ cụ thể
            sql = """
                    SELECT s.user_id, up.full_name, sp.student_code, s.gpa, s.total_credits
                    FROM semester s
                    JOIN user_profile up ON up.user_id = s.user_id
                    JOIN student_profile sp ON sp.user_id = s.user_id
                    WHERE s.semester_id = :semesterId
                       OR (s.academic_year = :academicYear AND s.semester_number =
                           (SELECT semester_number FROM semester WHERE semester_id = :semesterId LIMIT 1))
                    ORDER BY s.gpa DESC, s.total_credits DESC
                    """;
        } else {
            // Xếp hạng theo năm học — dùng GPA trung bình các kỳ
            sql = """
                    SELECT s.user_id, up.full_name, sp.student_code,
                           AVG(s.gpa) AS gpa, SUM(s.total_credits) AS total_credits
                    FROM semester s
                    JOIN user_profile up ON up.user_id = s.user_id
                    JOIN student_profile sp ON sp.user_id = s.user_id
                    WHERE s.academic_year = :academicYear
                    GROUP BY s.user_id, up.full_name, sp.student_code
                    ORDER BY gpa DESC, total_credits DESC
                    """;
        }

        var query = entityManager.createNativeQuery(sql);
        if (semesterId != null) {
            query.setParameter("semesterId", semesterId);
            query.setParameter("academicYear", targetYear != null ? targetYear : "");
        } else {
            query.setParameter("academicYear", targetYear != null ? targetYear : "");
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        AtomicInteger rank = new AtomicInteger(1);
        List<LeaderboardEntryDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long userId  = ((Number) row[0]).longValue();
            String name  = (String) row[1];
            String code  = (String) row[2];
            double gpa   = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            int credits  = row[4] != null ? ((Number) row[4]).intValue() : 0;

            result.add(LeaderboardEntryDto.builder()
                    .rank(rank.getAndIncrement())
                    .fullName(name)
                    .studentCode(code)
                    .initials(buildInitials(name))
                    .gpa(gpa)
                    .totalCredits(credits)
                    .isCurrentUser(userId.equals(currentUserId))
                    .build());
        }
        return result;
    }

    /** "Nguyễn Văn An" → "VA" (2 chữ cái cuối) */
    private String buildInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "??";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].length() >= 2
                    ? parts[0].substring(0, 2).toUpperCase()
                    : parts[0].toUpperCase();
        }
        return (String.valueOf(parts[parts.length - 2].charAt(0))
                + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  4. SCHOLARSHIPS
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public List<ScholarshipDto> getScholarships() {
        Long userId = currentUserId();
        List<Object[]> rows = scholarshipRepository.findAllWithStatusByUserId(userId);
        List<ScholarshipDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(ScholarshipDto.builder()
                    .scholarshipId(((Number) row[0]).longValue())
                    .name((String) row[1])
                    .organization((String) row[2])
                    .amount(row[3] != null ? ((Number) row[3]).longValue() : null)
                    .unit((String) row[4])
                    .minGpa(row[5] != null ? ((Number) row[5]).doubleValue() : null)
                    .description((String) row[6])
                    .status((String) row[7])
                    .receivedAt(row[8] != null ? row[8].toString() : null)
                    .build());
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  5. WARNINGS
    //  semesterId == null  → tất cả cảnh báo
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public List<AcademicWarningDto> getWarnings(Long semesterId) {
        Long userId = currentUserId();
        List<AcademicWarningEntity> list = semesterId != null
                ? warningRepository.findByUserIdAndSemesterIdOrderByIssuedAtDesc(userId, semesterId)
                : warningRepository.findByUserIdOrderByIssuedAtDesc(userId);

        List<AcademicWarningDto> result = new ArrayList<>();
        for (AcademicWarningEntity w : list) {
            result.add(AcademicWarningDto.builder()
                    .warningId(w.getWarningId())
                    .semesterId(w.getSemesterId())
                    .warningType(w.getWarningType())
                    .description(w.getDescription())
                    .issuedAt(w.getIssuedAt() != null ? w.getIssuedAt().toString() : null)
                    .resolvedAt(w.getResolvedAt() != null ? w.getResolvedAt().toString() : null)
                    .status(w.getStatus())
                    .build());
        }
        return result;
    }
}

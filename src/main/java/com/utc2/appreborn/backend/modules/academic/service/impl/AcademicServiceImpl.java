package com.utc2.appreborn.backend.modules.academic.service.impl;

import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.academic.dto.*;
import com.utc2.appreborn.backend.modules.academic.entity.AcademicWarningEntity;
import com.utc2.appreborn.backend.modules.academic.entity.SemesterEntity;
import com.utc2.appreborn.backend.modules.academic.repository.*;
import com.utc2.appreborn.backend.modules.academic.service.AcademicService;
import com.utc2.appreborn.backend.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicServiceImpl implements AcademicService {

    private final SemesterRepository      semesterRepository;
    private final EnrollmentRepository    enrollmentRepository;
    private final AcademicWarningRepository warningRepository;
    private final ScholarshipRepository   scholarshipRepository;
    private final LeaderboardRepository   leaderboardRepository;
    private final UserRepository          userRepository;

    // ─── Helper: lấy userId từ JWT trong SecurityContext ──────────────────────

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  1. SEMESTERS
    // ════════════════════════════════════════════════════════════════════════════

    @Override
    public List<SemesterDto> getSemesters() {
        Long userId = currentUserId();
        List<SemesterEntity> entities = semesterRepository.findByUserIdOrderBySemesterIdDesc(userId);

        List<SemesterDto> result = new ArrayList<>();
        for (SemesterEntity e : entities) {
            result.add(SemesterDto.builder()
                    .semesterId(e.getSemesterId())
                    .semesterName(e.getSemesterName())
                    .academicYear(e.getAcademicYear())
                    .semesterNumber(e.getSemesterNumber())
                    .startDate(e.getStartDate() != null ? e.getStartDate().toString() : null)
                    .endDate(e.getEndDate() != null ? e.getEndDate().toString() : null)
                    .gpa(e.getGpa())
                    .totalCredits(e.getTotalCredits())
                    .passedCredits(e.getPassedCredits())
                    .build());
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  2. GRADES
    // ════════════════════════════════════════════════════════════════════════════

    @Override
    public List<CourseGradeDto> getGrades(Long semesterId) {
        Long userId = currentUserId();

        List<Object[]> rows = (semesterId != null)
                ? enrollmentRepository.findGradesByUserIdAndSemesterId(userId, semesterId)
                : enrollmentRepository.findGradesByUserId(userId);

        // Column order from "SELECT e.*, c.course_code, c.course_name, c.credits, s.semester_name, s.academic_year":
        // 0=enrollment_id, 1=user_id, 2=course_id, 3=semester_id, 4=registered_at,
        // 5=status, 6=midterm_score, 7=final_score, 8=assignment_score, 9=total_score,
        // 10=letter_grade, 11=grade_point, 12=is_passed,
        // 13=course_code, 14=course_name, 15=credits, 16=semester_name, 17=academic_year
        List<CourseGradeDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(CourseGradeDto.builder()
                    .enrollmentId(toLong(row[0]))
                    .semesterId(toLong(row[3]))
                    .status(toString(row[5]))
                    .midtermScore(toDouble(row[6]))
                    .finalScore(toDouble(row[7]))
                    .assignmentScore(toDouble(row[8]))
                    .totalScore(toDouble(row[9]))
                    .letterGrade(toString(row[10]))
                    .gradePoint(toDouble(row[11]))
                    .isPassed(toBoolean(row[12]))
                    .courseCode(toString(row[13]))
                    .courseName(toString(row[14]))
                    .credits(toInt(row[15]))
                    .semesterName(toString(row[16]))
                    .academicYear(toString(row[17]))
                    .build());
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  3. LEADERBOARD
    // ════════════════════════════════════════════════════════════════════════════

    @Override
    public List<LeaderboardEntryDto> getLeaderboard(Long semesterId, String academicYear) {
        Long userId = currentUserId();

        List<Object[]> rows;
        if (semesterId != null) {
            rows = leaderboardRepository.findLeaderboardBySemester(semesterId);
        } else if (academicYear != null && !academicYear.isBlank()) {
            rows = leaderboardRepository.findLeaderboardByAcademicYear(academicYear);
        } else {
            // Default: năm học hiện tại của sinh viên
            List<SemesterEntity> semesters = semesterRepository.findByUserIdOrderBySemesterIdDesc(userId);
            String defaultYear = semesters.isEmpty() ? "" : semesters.get(0).getAcademicYear();
            rows = leaderboardRepository.findLeaderboardByAcademicYear(defaultYear);
        }

        List<LeaderboardEntryDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            // row: rank, student_code, full_name, gpa, total_credits, user_id
            String fullName = toString(row[2]);
            result.add(LeaderboardEntryDto.builder()
                    .rank(toInt(row[0]))
                    .studentCode(toString(row[1]))
                    .fullName(fullName)
                    .initials(buildInitials(fullName))
                    .gpa(toDouble(row[3]))
                    .totalCredits(toInt(row[4]))
                    .isCurrentUser(userId.equals(toLong(row[5])))
                    .build());
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  4. SCHOLARSHIPS
    // ════════════════════════════════════════════════════════════════════════════

    @Override
    public List<ScholarshipDto> getScholarships() {
        Long userId = currentUserId();
        List<Object[]> rows = scholarshipRepository.findAllWithStatusByUserId(userId);

        List<ScholarshipDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            // row: scholarship_id, name, organization, amount, unit, min_gpa,
            //      description, status, received_at
            result.add(ScholarshipDto.builder()
                    .scholarshipId(toLong(row[0]))
                    .name(toString(row[1]))
                    .organization(toString(row[2]))
                    .amount(row[3] != null ? ((java.math.BigDecimal) row[3]).longValue() : null)
                    .unit(toString(row[4]))
                    .minGpa(row[5] != null ? (java.math.BigDecimal) row[5] : null)
                    .description(toString(row[6]))
                    .status(toString(row[7]))
                    .receivedAt(row[8] != null ? row[8].toString() : null)
                    .build());
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  5. WARNINGS
    // ════════════════════════════════════════════════════════════════════════════

    @Override
    public List<AcademicWarningDto> getWarnings(Long semesterId) {
        Long userId = currentUserId();

        List<AcademicWarningEntity> entities = (semesterId != null)
                ? warningRepository.findByUserIdAndSemesterIdOrderByIssuedAtDesc(userId, semesterId)
                : warningRepository.findByUserIdOrderByIssuedAtDesc(userId);

        List<AcademicWarningDto> result = new ArrayList<>();
        for (AcademicWarningEntity e : entities) {
            result.add(AcademicWarningDto.builder()
                    .warningId(e.getWarningId())
                    .semesterId(e.getSemesterId())
                    .warningType(e.getWarningType())
                    .description(e.getDescription())
                    .issuedAt(e.getIssuedAt() != null ? e.getIssuedAt().toString() : null)
                    .resolvedAt(e.getResolvedAt() != null ? e.getResolvedAt().toString() : null)
                    .status(e.getStatus())
                    .build());
        }
        return result;
    }

    // ─── Conversion helpers ───────────────────────────────────────────────────

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Long l) return l;
        if (o instanceof Number n) return n.longValue();
        return Long.parseLong(o.toString());
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Integer i) return i;
        if (o instanceof Number n) return n.intValue();
        return Integer.parseInt(o.toString());
    }

    private Double toDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Double d) return d;
        if (o instanceof Number n) return n.doubleValue();
        return Double.parseDouble(o.toString());
    }

    private String toString(Object o) {
        return o != null ? o.toString() : null;
    }

    private Boolean toBoolean(Object o) {
        if (o == null) return null;
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue() == 1;
        return Boolean.parseBoolean(o.toString());
    }

    /** Lấy 2 chữ cái đầu của 2 từ cuối trong tên (giống logic của App) */
    private String buildInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "??";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            return String.valueOf(parts[parts.length - 2].charAt(0))
                    + parts[parts.length - 1].charAt(0);
        }
        return fullName.substring(0, Math.min(2, fullName.length())).toUpperCase();
    }
}
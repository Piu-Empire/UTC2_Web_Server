package com.utc2.appreborn.backend.modules.academic.service.impl;

import com.utc2.appreborn.backend.common.enums.Role;
import com.utc2.appreborn.backend.exception.ForbiddenException;
import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.academic.dto.*;
import com.utc2.appreborn.backend.modules.academic.entity.AcademicWarningEntity;
import com.utc2.appreborn.backend.modules.academic.entity.SemesterEntity;
import com.utc2.appreborn.backend.modules.academic.repository.AcademicWarningRepository;
import com.utc2.appreborn.backend.modules.academic.repository.ScholarshipRepository;
import com.utc2.appreborn.backend.modules.academic.repository.SemesterRepository;
import com.utc2.appreborn.backend.modules.academic.service.AcademicService;
import com.utc2.appreborn.backend.modules.auth.entity.User;
import com.utc2.appreborn.backend.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
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

    // ── Auth helpers ──────────────────────────────────────────────────────

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Long resolveUserId(Long userId) {
        if (userId != null) return userId;
        return currentUser().getId();
    }

    private void requireRole(Role... roles) {
        User u = currentUser();
        for (Role r : roles) {
            if (u.getRole() == r) return;
        }
        throw new ForbiddenException("Không có quyền thực hiện thao tác này");
    }

    private void requireStaffLevel(int level) {
        User u = currentUser();
        if (u.getRole() != Role.STAFF || u.getStaffLevel() == null || u.getStaffLevel() != level) {
            throw new ForbiddenException("Chỉ STAFF level " + level + " mới có quyền này");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  1. SEMESTERS
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public List<SemesterDto> getSemesters(Long userId) {
        Long id = resolveUserId(userId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT s.semester_id, s.semester_name, s.academic_year, s.semester_number,
                       s.start_date, s.end_date, s.gpa, s.total_credits, s.passed_credits,
                       up.full_name, sp.student_code
                FROM semester s
                LEFT JOIN user_profile up ON up.user_id = s.user_id
                LEFT JOIN student_profile sp ON sp.user_id = s.user_id
                WHERE s.user_id = :userId
                ORDER BY s.semester_number ASC
                """)
                .setParameter("userId", id)
                .getResultList();

        List<SemesterDto> result = new ArrayList<>();
        for (Object[] r : rows) {
            result.add(SemesterDto.builder()
                    .semesterId(((Number) r[0]).longValue())
                    .semesterName((String) r[1])
                    .academicYear((String) r[2])
                    .semesterNumber(r[3] != null ? ((Number) r[3]).intValue() : null)
                    .startDate(r[4] != null ? r[4].toString() : null)
                    .endDate(r[5] != null ? r[5].toString() : null)
                    .gpa(r[6] != null ? ((Number) r[6]).doubleValue() : null)
                    .totalCredits(r[7] != null ? ((Number) r[7]).intValue() : null)
                    .passedCredits(r[8] != null ? ((Number) r[8]).intValue() : null)
                    .fullName((String) r[9])
                    .studentCode((String) r[10])
                    .build());
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  2. GRADES — sinh viên xem điểm của mình
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public List<CourseGradeDto> getGrades(Long userId, Long semesterId) {
        Long id = resolveUserId(userId);

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

        var query = entityManager.createNativeQuery(sql).setParameter("userId", id);
        if (semesterId != null) query.setParameter("semesterId", semesterId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return mapToGradeDto(rows);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  GRADES BY COURSE — giảng viên xem danh sách sinh viên của môn + lớp
    //  Quyền: STAFF lv2 hoặc ADMIN
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public List<GradesByCourseDto> getGradesByCourse(Long courseId, String className) {
        User caller = currentUser();
        boolean isAdmin    = caller.getRole() == Role.ADMIN;
        boolean isTeacher  = caller.getRole() == Role.STAFF
                          && caller.getStaffLevel() != null
                          && caller.getStaffLevel() == 2;
        if (!isAdmin && !isTeacher) {
            throw new ForbiddenException("Chỉ giảng viên (STAFF lv2) hoặc Admin mới có quyền này");
        }

        String sql = """
                SELECT e.enrollment_id, e.user_id,
                       sp.student_code, up.full_name, sp.class_name,
                       c.credits,
                       e.midterm_score, e.final_score, e.assignment_score, e.total_score,
                       e.letter_grade, e.grade_point, e.is_passed, e.status
                FROM enrollment e
                JOIN course c ON c.course_id = e.course_id
                JOIN student_profile sp ON sp.user_id = e.user_id
                JOIN user_profile up ON up.user_id = e.user_id
                WHERE e.course_id = :courseId
                """ +
                (className != null && !className.isBlank() ? " AND sp.class_name = :className" : "") +
                " ORDER BY sp.student_code ASC";

        var query = entityManager.createNativeQuery(sql).setParameter("courseId", courseId);
        if (className != null && !className.isBlank()) query.setParameter("className", className);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<GradesByCourseDto> result = new ArrayList<>();
        for (Object[] r : rows) {
            result.add(GradesByCourseDto.builder()
                    .enrollmentId(((Number) r[0]).longValue())
                    .userId(((Number) r[1]).longValue())
                    .studentCode((String) r[2])
                    .fullName((String) r[3])
                    .className((String) r[4])
                    .credits(r[5] != null ? ((Number) r[5]).intValue() : null)
                    .midtermScore(r[6] != null ? ((Number) r[6]).doubleValue() : null)
                    .finalScore(r[7] != null ? ((Number) r[7]).doubleValue() : null)
                    .assignmentScore(r[8] != null ? ((Number) r[8]).doubleValue() : null)
                    .totalScore(r[9] != null ? ((Number) r[9]).doubleValue() : null)
                    .letterGrade((String) r[10])
                    .gradePoint(r[11] != null ? ((Number) r[11]).doubleValue() : null)
                    .isPassed(r[12] != null ? (Boolean) r[12] : null)
                    .status((String) r[13])
                    .build());
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UPDATE GRADE — Quyền: STAFF lv2 hoặc ADMIN
    // ════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public CourseGradeDto updateGrade(Long enrollmentId, GradeUpdateDto dto) {
        User caller = currentUser();
        boolean isAdmin   = caller.getRole() == Role.ADMIN;
        boolean isTeacher = caller.getRole() == Role.STAFF
                         && caller.getStaffLevel() != null
                         && caller.getStaffLevel() == 2;
        if (!isAdmin && !isTeacher) {
            throw new ForbiddenException("Chỉ giảng viên (STAFF lv2) hoặc Admin mới được nhập điểm");
        }

        double bt  = dto.getAssignmentScore() != null ? dto.getAssignmentScore() : 0.0;
        double gk  = dto.getMidtermScore()    != null ? dto.getMidtermScore()    : 0.0;
        double ck  = dto.getFinalScore()      != null ? dto.getFinalScore()      : 0.0;

        Double  total  = null;
        String  letter = null;
        Double  gp     = null;
        Boolean passed = null;

        if (dto.getMidtermScore() != null && dto.getFinalScore() != null) {
            total  = Math.round((bt * 0.3 + gk * 0.3 + ck * 0.4) * 100.0) / 100.0;
            letter = calcLetterGrade(total);
            gp     = calcGradePoint(total);
            passed = total >= 5.0;
        }

        entityManager.createNativeQuery(
                "UPDATE enrollment SET midterm_score=:m, final_score=:f, assignment_score=:a, " +
                "total_score=:t, letter_grade=:l, grade_point=:gp, is_passed=:p WHERE enrollment_id=:id")
                .setParameter("m", dto.getMidtermScore()).setParameter("f", dto.getFinalScore())
                .setParameter("a", dto.getAssignmentScore()).setParameter("t", total)
                .setParameter("l", letter).setParameter("gp", gp).setParameter("p", passed)
                .setParameter("id", enrollmentId).executeUpdate();

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(
                "SELECT e.enrollment_id, c.course_code, c.course_name, c.credits, " +
                "e.midterm_score, e.final_score, e.assignment_score, e.total_score, " +
                "e.letter_grade, e.grade_point, e.is_passed, e.status, " +
                "s.semester_id, s.semester_name, s.academic_year " +
                "FROM enrollment e JOIN course c ON c.course_id=e.course_id " +
                "JOIN semester s ON s.semester_id=e.semester_id WHERE e.enrollment_id=:id")
                .setParameter("id", enrollmentId).getResultList();

        if (rows.isEmpty()) throw new ResourceNotFoundException("Enrollment not found: " + enrollmentId);
        return mapToGradeDto(rows).get(0);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  3. LEADERBOARD
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public List<LeaderboardEntryDto> getLeaderboard(Long semesterId, String academicYear) {
        Long currentUserId = resolveUserId(null);
        String targetYear  = academicYear;
        if (targetYear == null && semesterId != null) {
            targetYear = semesterRepository.findById(semesterId)
                    .map(SemesterEntity::getAcademicYear).orElse(null);
        }

        String sql = semesterId != null
            ? """
              SELECT s.user_id, up.full_name, sp.student_code, s.gpa, s.total_credits
              FROM semester s
              JOIN user_profile up ON up.user_id = s.user_id
              JOIN student_profile sp ON sp.user_id = s.user_id
              WHERE s.semester_id = :semesterId
              ORDER BY s.gpa DESC, s.total_credits DESC
              """
            : """
              SELECT s.user_id, up.full_name, sp.student_code,
                     AVG(s.gpa) AS gpa, SUM(s.total_credits) AS total_credits
              FROM semester s
              JOIN user_profile up ON up.user_id = s.user_id
              JOIN student_profile sp ON sp.user_id = s.user_id
              WHERE s.academic_year = :academicYear
              GROUP BY s.user_id, up.full_name, sp.student_code
              ORDER BY gpa DESC, total_credits DESC
              """;

        var query = entityManager.createNativeQuery(sql);
        if (semesterId != null) query.setParameter("semesterId", semesterId);
        else query.setParameter("academicYear", targetYear != null ? targetYear : "");

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        AtomicInteger rank = new AtomicInteger(1);
        List<LeaderboardEntryDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long entryUserId = row[0] != null ? ((Number) row[0]).longValue() : null;
            String name = (String) row[1];
            result.add(LeaderboardEntryDto.builder()
                    .rank(rank.getAndIncrement())
                    .fullName(name)
                    .studentCode((String) row[2])
                    .initials(buildInitials(name))
                    .gpa(row[3] != null ? ((Number) row[3]).doubleValue() : 0.0)
                    .totalCredits(row[4] != null ? ((Number) row[4]).intValue() : 0)
                    .isCurrentUser(entryUserId != null && entryUserId.equals(currentUserId))
                    .build());
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  4. SCHOLARSHIPS
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public List<ScholarshipDto> getScholarships(Long userId) {
        Long id = resolveUserId(userId);
        List<Object[]> rows = scholarshipRepository.findAllWithStatusByUserId(id);
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

    // ── ADVISOR: cập nhật trạng thái học bổng cho sinh viên ─────────────

    @Override
    @Transactional
    public ScholarshipDto updateScholarshipStatus(ScholarshipUpsertDto dto) {
        User caller = currentUser();
        if (caller.getRole() != Role.ADVISOR && caller.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Chỉ cố vấn học tập hoặc Admin mới có quyền này");
        }

        // Upsert student_scholarship
        int updated = entityManager.createNativeQuery(
                "UPDATE student_scholarship SET status=:status, received_at=:receivedAt " +
                "WHERE user_id=:userId AND scholarship_id=:scholarshipId")
                .setParameter("status",       dto.getStatus())
                .setParameter("receivedAt",   dto.getReceivedAt())
                .setParameter("userId",       dto.getUserId())
                .setParameter("scholarshipId",dto.getScholarshipId())
                .executeUpdate();

        if (updated == 0) {
            entityManager.createNativeQuery(
                    "INSERT INTO student_scholarship (user_id, scholarship_id, status, semester_id, received_at) " +
                    "VALUES (:userId, :scholarshipId, :status, :semesterId, :receivedAt)")
                    .setParameter("userId",       dto.getUserId())
                    .setParameter("scholarshipId",dto.getScholarshipId())
                    .setParameter("status",       dto.getStatus())
                    .setParameter("semesterId",   dto.getSemesterId())
                    .setParameter("receivedAt",   dto.getReceivedAt())
                    .executeUpdate();
        }

        List<ScholarshipDto> list = getScholarships(dto.getUserId());
        return list.stream()
                .filter(s -> s.getScholarshipId().equals(dto.getScholarshipId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Scholarship not found"));
    }

    // ════════════════════════════════════════════════════════════════════════
    //  5. WARNINGS
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public List<AcademicWarningDto> getWarnings(Long userId, Long semesterId) {
        Long id = resolveUserId(userId);
        List<AcademicWarningEntity> list = semesterId != null
                ? warningRepository.findByUserIdAndSemesterIdOrderByIssuedAtDesc(id, semesterId)
                : warningRepository.findByUserIdOrderByIssuedAtDesc(id);

        List<AcademicWarningDto> result = new ArrayList<>();
        for (AcademicWarningEntity w : list) {
            result.add(mapWarningToDto(w));
        }
        return result;
    }

    // ── ADVISOR: tạo/cập nhật cảnh báo ──────────────────────────────────

    @Override
    @Transactional
    public AcademicWarningDto upsertWarning(WarningUpsertDto dto) {
        User caller = currentUser();
        if (caller.getRole() != Role.ADVISOR && caller.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Chỉ cố vấn học tập hoặc Admin mới có quyền này");
        }

        AcademicWarningEntity entity = AcademicWarningEntity.builder()
                .userId(dto.getUserId())
                .semesterId(dto.getSemesterId())
                .warningType(dto.getWarningType())
                .description(dto.getDescription())
                .issuedAt(LocalDateTime.now())
                .resolvedAt(dto.getResolvedAt() != null
                        ? LocalDateTime.parse(dto.getResolvedAt()) : null)
                .status(dto.getStatus() != null ? dto.getStatus() : "ACTIVE")
                .build();

        AcademicWarningEntity saved = warningRepository.save(entity);
        return mapWarningToDto(saved);
    }

    @Override
    @Transactional
    public void deleteWarning(Long warningId) {
        User caller = currentUser();
        if (caller.getRole() != Role.ADVISOR && caller.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Chỉ cố vấn học tập hoặc Admin mới có quyền này");
        }
        warningRepository.deleteById(warningId);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════════════════════════

    private List<CourseGradeDto> mapToGradeDto(List<Object[]> rows) {
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

    private AcademicWarningDto mapWarningToDto(AcademicWarningEntity w) {
        return AcademicWarningDto.builder()
                .warningId(w.getWarningId())
                .semesterId(w.getSemesterId())
                .warningType(w.getWarningType())
                .description(w.getDescription())
                .issuedAt(w.getIssuedAt() != null ? w.getIssuedAt().toString() : null)
                .resolvedAt(w.getResolvedAt() != null ? w.getResolvedAt().toString() : null)
                .status(w.getStatus())
                .build();
    }

    private String buildInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "??";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1)
            return parts[0].length() >= 2 ? parts[0].substring(0, 2).toUpperCase() : parts[0].toUpperCase();
        return (String.valueOf(parts[parts.length - 2].charAt(0))
                + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private String calcLetterGrade(double t) {
        if (t >= 9.0) return "A+"; if (t >= 8.5) return "A"; if (t >= 8.0) return "B+";
        if (t >= 7.0) return "B";  if (t >= 6.5) return "C+"; if (t >= 5.5) return "C";
        if (t >= 5.0) return "D+"; if (t >= 4.0) return "D"; return "F";
    }

    private double calcGradePoint(double t) {
        if (t >= 8.5) return 4.0; if (t >= 8.0) return 3.5; if (t >= 7.0) return 3.0;
        if (t >= 6.5) return 2.5; if (t >= 5.5) return 2.0; if (t >= 5.0) return 1.5;
        if (t >= 4.0) return 1.0; return 0.0;
    }
}
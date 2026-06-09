package com.utc2.appreborn.backend.modules.academic.service.impl;

import com.utc2.appreborn.backend.common.enums.Role;
import com.utc2.appreborn.backend.exception.ForbiddenException;
import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.academic.dto.*;
import com.utc2.appreborn.backend.modules.academic.entity.*;
import com.utc2.appreborn.backend.modules.academic.repository.*;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class AcademicServiceImpl implements AcademicService {

    private final SemesterRepository           semesterRepository;
    private final AcademicWarningRepository    warningRepository;
    private final ScholarshipRepository        scholarshipRepository;
    private final UserRepository               userRepository;
    private final TeacherCourseRepository      teacherCourseRepository;
    private final LeaderboardApprovalRepository leaderboardApprovalRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // ── Auth helpers ──────────────────────────────────────────────────────

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Long resolveUserId(Long userId) {
        return userId != null ? userId : currentUser().getId();
    }

    private boolean isAdminOrLv5() {
        User u = currentUser();
        return u.getRole() == Role.ADMIN
            || (u.getRole() == Role.STAFF && u.getStaffLevel() != null && u.getStaffLevel() >= 5);
    }

    private boolean isAdvisorOrLv5() {
        User u = currentUser();
        return u.getRole() == Role.ADMIN
            || u.getRole() == Role.ADVISOR
            || (u.getRole() == Role.STAFF && u.getStaffLevel() != null && u.getStaffLevel() >= 5);
    }

    /** lv3+ hoặc Advisor: có quyền thêm học bổng / cảnh báo học vụ */
    private boolean canAddScholarshipOrWarning() {
        User u = currentUser();
        return u.getRole() == Role.ADMIN
            || u.getRole() == Role.ADVISOR
            || (u.getRole() == Role.STAFF && u.getStaffLevel() != null && u.getStaffLevel() >= 3);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  1. SEMESTERS
    // ════════════════════════════════════════════════════════════════════════

    private Long resolveUserIdByCode(Long userId, String studentCode) {
        if (userId != null) return userId;
        if (studentCode == null || studentCode.isBlank()) throw new ResourceNotFoundException("Thiếu mssv");
        try {
            Number id = (Number) entityManager.createNativeQuery("SELECT user_id FROM student_profile WHERE student_code = :code")
                    .setParameter("code", studentCode.trim())
                    .getSingleResult();
            if (id == null) throw new ResourceNotFoundException("Không tìm thấy sinh viên với MSSV: " + studentCode);
            return id.longValue();
        } catch (jakarta.persistence.NoResultException e) {
            throw new ResourceNotFoundException("Không tìm thấy sinh viên với MSSV: " + studentCode);
        }
    }

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
                WHERE s.user_id = :userId ORDER BY s.semester_number ASC
                """).setParameter("userId", id).getResultList();

        List<SemesterDto> result = new ArrayList<>();
        for (Object[] r : rows) {
            result.add(SemesterDto.builder()
                    .semesterId(((Number)r[0]).longValue()).semesterName((String)r[1])
                    .academicYear((String)r[2]).semesterNumber(r[3]!=null?((Number)r[3]).intValue():null)
                    .startDate(r[4]!=null?r[4].toString():null).endDate(r[5]!=null?r[5].toString():null)
                    .gpa(r[6]!=null?((Number)r[6]).doubleValue():null)
                    .totalCredits(r[7]!=null?((Number)r[7]).intValue():null)
                    .passedCredits(r[8]!=null?((Number)r[8]).intValue():null)
                    .fullName((String)r[9]).studentCode((String)r[10]).build());
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  2. GRADES — chỉ trả môn status = 'hoàn thành'
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
                WHERE e.user_id = :userId AND e.status = 'hoàn thành'
                """ + (semesterId != null ? " AND e.semester_id = :semesterId" : "")
                + " ORDER BY s.semester_number ASC, c.course_code ASC";

        var query = entityManager.createNativeQuery(sql).setParameter("userId", id);
        if (semesterId != null) query.setParameter("semesterId", semesterId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return mapToGradeDto(rows);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  GRADES BY COURSE — tất cả role xem được, giảng viên chỉ xem môn mình dạy
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public List<GradesByCourseDto> getGradesByCourse(Long courseId, String className) {
        User caller = currentUser();
        boolean isTeacher = caller.getRole() == Role.STAFF
                         && caller.getStaffLevel() != null && caller.getStaffLevel() == 2;
        if (isTeacher && !teacherCourseRepository.isAssigned(caller.getId(), courseId, className))
            throw new ForbiddenException("Bạn không được phân công dạy môn hoặc lớp này");

        String sql = """
                SELECT e.enrollment_id, e.user_id,
                       sp.student_code, up.full_name, sp.class_name,
                       c.credits, e.midterm_score, e.final_score, e.assignment_score,
                       e.total_score, e.letter_grade, e.grade_point, e.is_passed, e.status
                FROM enrollment e
                JOIN course c ON c.course_id = e.course_id
                JOIN student_profile sp ON sp.user_id = e.user_id
                JOIN user_profile up ON up.user_id = e.user_id
                WHERE e.course_id = :courseId
                """ + (className != null && !className.isBlank() ? " AND sp.class_name = :className" : "")
                + " ORDER BY sp.student_code ASC";

        var query = entityManager.createNativeQuery(sql).setParameter("courseId", courseId);
        if (className != null && !className.isBlank()) query.setParameter("className", className);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<GradesByCourseDto> result = new ArrayList<>();
        for (Object[] r : rows) {
            result.add(GradesByCourseDto.builder()
                    .enrollmentId(((Number)r[0]).longValue()).userId(((Number)r[1]).longValue())
                    .studentCode((String)r[2]).fullName((String)r[3]).className((String)r[4])
                    .credits(r[5]!=null?((Number)r[5]).intValue():null)
                    .midtermScore(r[6]!=null?((Number)r[6]).doubleValue():null)
                    .finalScore(r[7]!=null?((Number)r[7]).doubleValue():null)
                    .assignmentScore(r[8]!=null?((Number)r[8]).doubleValue():null)
                    .totalScore(r[9]!=null?((Number)r[9]).doubleValue():null)
                    .letterGrade((String)r[10])
                    .gradePoint(r[11]!=null?((Number)r[11]).doubleValue():null)
                    .isPassed(r[12]!=null?(Boolean)r[12]:null).status((String)r[13]).build());
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UPDATE GRADE — chỉ lv2 + admin
    // ════════════════════════════════════════════════════════════════════════

    @Override @Transactional
    public CourseGradeDto updateGrade(Long enrollmentId, GradeUpdateDto dto) {
        User caller = currentUser();
        boolean isAdmin   = caller.getRole() == Role.ADMIN;
        boolean isTeacher = caller.getRole() == Role.STAFF
                         && caller.getStaffLevel() != null && caller.getStaffLevel() == 2;
        if (!isAdmin && !isTeacher)
            throw new ForbiddenException("Chỉ giảng viên (STAFF lv2) hoặc Admin mới được nhập điểm");
        if (isTeacher && !teacherCourseRepository.canUpdateEnrollment(caller.getId(), enrollmentId))
            throw new ForbiddenException("Bạn không có quyền nhập điểm cho sinh viên này");

        double bt = dto.getAssignmentScore()!=null?dto.getAssignmentScore():0.0;
        double gk = dto.getMidtermScore()!=null?dto.getMidtermScore():0.0;
        double ck = dto.getFinalScore()!=null?dto.getFinalScore():0.0;
        Double total=null; String letter=null; Double gp=null; Boolean passed=null;
        if (dto.getMidtermScore()!=null && dto.getFinalScore()!=null) {
            total  = Math.round((bt*0.3+gk*0.3+ck*0.4)*100.0)/100.0;
            letter = calcLetterGrade(total); gp = calcGradePoint(total); passed = total>=5.0;
        }
        entityManager.createNativeQuery(
            "UPDATE enrollment SET midterm_score=:m,final_score=:f,assignment_score=:a," +
            "total_score=:t,letter_grade=:l,grade_point=:gp,is_passed=:p WHERE enrollment_id=:id")
            .setParameter("m",dto.getMidtermScore()).setParameter("f",dto.getFinalScore())
            .setParameter("a",dto.getAssignmentScore()).setParameter("t",total)
            .setParameter("l",letter).setParameter("gp",gp).setParameter("p",passed)
            .setParameter("id",enrollmentId).executeUpdate();

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(
            "SELECT e.enrollment_id,c.course_code,c.course_name,c.credits," +
            "e.midterm_score,e.final_score,e.assignment_score,e.total_score," +
            "e.letter_grade,e.grade_point,e.is_passed,e.status," +
            "s.semester_id,s.semester_name,s.academic_year " +
            "FROM enrollment e JOIN course c ON c.course_id=e.course_id " +
            "JOIN semester s ON s.semester_id=e.semester_id WHERE e.enrollment_id=:id")
            .setParameter("id",enrollmentId).getResultList();
        if (rows.isEmpty()) throw new ResourceNotFoundException("Enrollment not found: "+enrollmentId);
        return mapToGradeDto(rows).get(0);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  3. LEADERBOARD
    //  App chỉ thấy kỳ đã được duyệt
    //  Xếp hạng: xếp loại (kết hợp GPA + ĐRL) → GPA → credits
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public List<LeaderboardEntryDto> getLeaderboard(Long semesterId, String academicYear, String className) {
        Long currentUserId = resolveUserId(null);
        boolean isAdminOrLv5 = isAdminOrLv5();

        String targetYear = academicYear;
        String targetSemesterName = null;
        
        if (semesterId != null) {
            Optional<SemesterEntity> semOpt = semesterRepository.findById(semesterId);
            if (semOpt.isPresent()) {
                targetSemesterName = semOpt.get().getSemesterName();
                if (targetYear == null) targetYear = semOpt.get().getAcademicYear();
            }
        }

        // Nếu là App (không phải admin/lv5), chỉ trả kỳ đã duyệt
        if (!isAdminOrLv5 && targetSemesterName != null) {
            boolean isApproved = !entityManager.createNativeQuery(
                "SELECT 1 FROM leaderboard_approval la JOIN semester s ON la.semester_id = s.semester_id WHERE s.semester_name = :semName")
                .setParameter("semName", targetSemesterName)
                .getResultList().isEmpty();
            if (!isApproved) return Collections.emptyList();
        }

        boolean filterClass = className != null && !className.isBlank();

        String sql = targetSemesterName != null
            ? """
              SELECT s.user_id, up.full_name, sp.student_code, s.gpa, s.total_credits
              FROM semester s
              JOIN user_profile up ON up.user_id=s.user_id
              JOIN student_profile sp ON sp.user_id=s.user_id
              WHERE s.semester_name=:semesterName
              """ + (filterClass ? " AND sp.class_name=:className" : "") + """
              ORDER BY s.gpa DESC, s.total_credits DESC
              """
            : """
              SELECT s.user_id, up.full_name, sp.student_code,
                     AVG(s.gpa) AS gpa, SUM(s.total_credits) AS total_credits
              FROM semester s
              JOIN user_profile up ON up.user_id=s.user_id
              JOIN student_profile sp ON sp.user_id=s.user_id
              WHERE s.academic_year=:academicYear
              """ + (filterClass ? " AND sp.class_name=:className" : "") + """
              GROUP BY s.user_id,up.full_name,sp.student_code
              ORDER BY gpa DESC, total_credits DESC
              """;

        var query = entityManager.createNativeQuery(sql);
        if (targetSemesterName != null) query.setParameter("semesterName", targetSemesterName);
        else query.setParameter("academicYear", targetYear != null ? targetYear : "");
        if (filterClass) query.setParameter("className", className);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        AtomicInteger rank = new AtomicInteger(1);
        List<LeaderboardEntryDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long uid   = row[0]!=null?((Number)row[0]).longValue():null;
            String name= (String)row[1];
            double gpa = row[3]!=null?((Number)row[3]).doubleValue():0.0;
            result.add(LeaderboardEntryDto.builder()
                    .rank(rank.getAndIncrement()).fullName(name).studentCode((String)row[2])
                    .initials(buildInitials(name)).gpa(gpa)
                    .totalCredits(row[4]!=null?((Number)row[4]).intValue():0)
                    .isCurrentUser(uid!=null&&uid.equals(currentUserId))
                    .build());
        }
        return result;
    }

    @Override @Transactional
    public void approveLeaderboard(Long semesterId) {
        if (!isAdminOrLv5()) throw new ForbiddenException("Chỉ Admin hoặc Staff lv5 mới có quyền duyệt");
        if (!leaderboardApprovalRepository.existsBySemesterId(semesterId)) {
            leaderboardApprovalRepository.save(LeaderboardApprovalEntity.builder()
                    .semesterId(semesterId).approvedBy(currentUser().getId())
                    .approvedAt(LocalDateTime.now()).build());
        }
    }

    @Override @Transactional
    public void revokeLeaderboard(Long semesterId) {
        if (!isAdminOrLv5()) throw new ForbiddenException("Chỉ Admin hoặc Staff lv5 mới có quyền này");
        leaderboardApprovalRepository.findBySemesterId(semesterId)
                .ifPresent(leaderboardApprovalRepository::delete);
    }

    @Override
    public List<LeaderboardEntryDto> getPendingLeaderboard(Long semesterId, String academicYear) {
        if (!isAdminOrLv5()) throw new ForbiddenException("Không có quyền");
        // Trả về leaderboard chưa duyệt (bỏ check approval)
        Long currentUserId = resolveUserId(null);
        String targetYear = academicYear;
        if (targetYear==null && semesterId!=null)
            targetYear = semesterRepository.findById(semesterId).map(SemesterEntity::getAcademicYear).orElse(null);

        String sql = semesterId!=null
            ? "SELECT s.user_id,up.full_name,sp.student_code,s.gpa,s.total_credits FROM semester s JOIN user_profile up ON up.user_id=s.user_id JOIN student_profile sp ON sp.user_id=s.user_id WHERE s.semester_id=:sid ORDER BY s.gpa DESC"
            : "SELECT s.user_id,up.full_name,sp.student_code,AVG(s.gpa),SUM(s.total_credits) FROM semester s JOIN user_profile up ON up.user_id=s.user_id JOIN student_profile sp ON sp.user_id=s.user_id WHERE s.academic_year=:ay GROUP BY s.user_id,up.full_name,sp.student_code ORDER BY AVG(s.gpa) DESC";
        var query = entityManager.createNativeQuery(sql);
        if (semesterId!=null) query.setParameter("sid",semesterId);
        else query.setParameter("ay",targetYear!=null?targetYear:"");
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        AtomicInteger rank = new AtomicInteger(1);
        List<LeaderboardEntryDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long uid=(row[0]!=null?((Number)row[0]).longValue():null);
            String name=(String)row[1];
            double gpa=row[3]!=null?((Number)row[3]).doubleValue():0.0;
            result.add(LeaderboardEntryDto.builder().rank(rank.getAndIncrement())
                    .fullName(name).studentCode((String)row[2]).initials(buildInitials(name))
                    .gpa(gpa).totalCredits(row[4]!=null?((Number)row[4]).intValue():0)
                    .isCurrentUser(uid!=null&&uid.equals(currentUserId)).build());
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  4. SCHOLARSHIPS
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public List<ScholarshipDto> getScholarships(Long userId) {
        Long id = resolveUserId(userId);
        boolean admin = isAdminOrLv5() || isAdvisorOrLv5();
        List<Object[]> rows = admin
            ? scholarshipRepository.findAllWithStatusByUserId(id)
            : scholarshipRepository.findApprovedByUserId(id);
        return mapScholarship(rows);
    }

    @Override @Transactional
    public ScholarshipDto updateScholarshipStatus(ScholarshipUpsertDto dto) {
        if (!canAddScholarshipOrWarning()) throw new ForbiddenException("Chỉ Staff lv3+ hoặc Advisor mới có quyền thêm học bổng");
        Long targetUserId = resolveUserIdByCode(dto.getUserId(), dto.getStudentCode());

        int updated = entityManager.createNativeQuery(
            "UPDATE student_scholarship SET pending_status='pending',received_at=:receivedAt WHERE user_id=:uid AND scholarship_id=:sid")
            .setParameter("receivedAt",dto.getReceivedAt()).setParameter("uid",targetUserId)
            .setParameter("sid",dto.getScholarshipId()).executeUpdate();
        if (updated==0)
            entityManager.createNativeQuery(
                "INSERT INTO student_scholarship(user_id,scholarship_id,pending_status,semester_id,received_at) VALUES(:uid,:sid,'pending',:semId,:recAt)")
                .setParameter("uid",targetUserId).setParameter("sid",dto.getScholarshipId())
                .setParameter("semId",dto.getSemesterId()).setParameter("recAt",dto.getReceivedAt()).executeUpdate();
        return getScholarships(targetUserId).stream()
                .filter(s->s.getScholarshipId().equals(dto.getScholarshipId())).findFirst()
                .orElseThrow(()->new ResourceNotFoundException("Scholarship not found"));
    }

    @Override @Transactional
    public ScholarshipDto approveScholarship(Long userId, Long scholarshipId) {
        if (!isAdminOrLv5()) throw new ForbiddenException("Chỉ Admin hoặc Staff lv5 mới có quyền duyệt");
        entityManager.createNativeQuery(
            "UPDATE student_scholarship SET pending_status='approved',approved_at=NOW() WHERE user_id=:uid AND scholarship_id=:sid")
            .setParameter("uid",userId).setParameter("sid",scholarshipId).executeUpdate();
        return getScholarships(userId).stream().filter(s->s.getScholarshipId().equals(scholarshipId))
                .findFirst().orElseThrow(()->new ResourceNotFoundException("Scholarship not found"));
    }

    @Override @Transactional
    public ScholarshipDto markScholarshipReceived(Long userId, Long scholarshipId) {
        if (!isAdminOrLv5()) throw new ForbiddenException("Chỉ Admin hoặc Staff lv5 mới có quyền này");
        entityManager.createNativeQuery(
            "UPDATE student_scholarship SET pending_status='received',received_at=NOW() WHERE user_id=:uid AND scholarship_id=:sid AND pending_status='approved'")
            .setParameter("uid",userId).setParameter("sid",scholarshipId).executeUpdate();
        return getScholarships(userId).stream().filter(s->s.getScholarshipId().equals(scholarshipId))
                .findFirst().orElseThrow(()->new ResourceNotFoundException("Scholarship not found"));
    }

    @Override
    public List<Object> getPendingScholarships() {
        if (!isAdminOrLv5()) throw new ForbiddenException("Không có quyền");
        return new ArrayList<>(scholarshipRepository.findPendingApprovals());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  5. WARNINGS — on-the-fly expire sau 6 tháng kể từ approved_at
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public List<AcademicWarningDto> getWarnings(Long userId, Long semesterId) {
        Long id = resolveUserId(userId);
        boolean admin = isAdminOrLv5() || isAdvisorOrLv5();
        List<AcademicWarningEntity> list = admin
            ? (semesterId!=null
                ? warningRepository.findByUserIdAndSemesterIdOrderByIssuedAtDesc(id,semesterId)
                : warningRepository.findByUserIdOrderByIssuedAtDesc(id))
            : (semesterId!=null
                ? warningRepository.findApprovedByUserIdAndSemesterId(id,semesterId)
                : warningRepository.findApprovedByUserId(id));
        return list.stream().map(this::mapWarningToDto).toList();
    }

    @Override @Transactional
    public AcademicWarningDto upsertWarning(WarningUpsertDto dto) {
        if (!canAddScholarshipOrWarning()) throw new ForbiddenException("Chỉ Staff lv3+ hoặc Advisor mới có quyền thêm cảnh báo");
        Long targetUserId = resolveUserIdByCode(dto.getUserId(), dto.getStudentCode());

        AcademicWarningEntity e = AcademicWarningEntity.builder()
                .userId(targetUserId).semesterId(dto.getSemesterId())
                .warningType(dto.getWarningType()).description(dto.getDescription())
                .issuedAt(LocalDateTime.now()).status("pending") // chưa duyệt
                .build();
        return mapWarningToDto(warningRepository.save(e));
    }

    @Override @Transactional
    public void deleteWarning(Long warningId) {
        if (!canAddScholarshipOrWarning()) throw new ForbiddenException("Chỉ Staff lv3+ hoặc Advisor mới có quyền xoá cảnh báo");
        warningRepository.deleteById(warningId);
    }

    @Override @Transactional
    public AcademicWarningDto approveWarning(Long warningId) {
        if (!isAdminOrLv5()) throw new ForbiddenException("Chỉ Admin hoặc Staff lv5 mới có quyền duyệt");
        AcademicWarningEntity w = warningRepository.findById(warningId)
                .orElseThrow(()->new ResourceNotFoundException("Warning not found: "+warningId));
        w.setApprovedBy(currentUser().getId());
        w.setApprovedAt(LocalDateTime.now());
        w.setStatus("ACTIVE");
        return mapWarningToDto(warningRepository.save(w));
    }

    @Override
    public List<AcademicWarningDto> getPendingWarnings() {
        if (!isAdminOrLv5()) throw new ForbiddenException("Không có quyền");
        return warningRepository.findByApprovedAtIsNullOrderByIssuedAtDesc()
                .stream().map(this::mapWarningToDto).toList();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  TEACHER COURSE
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public List<TeacherCourseDto> getMyTeacherCourses() {
        Long userId = currentUser().getId();
        List<TeacherCourseEntity> list = teacherCourseRepository.findByUserId(userId);
        List<TeacherCourseDto> result = new ArrayList<>();
        for (TeacherCourseEntity tc : list) {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = entityManager.createNativeQuery(
                "SELECT c.course_code,c.course_name,s.semester_name FROM course c,semester s WHERE c.course_id=:cid AND s.semester_id=:sid")
                .setParameter("cid",tc.getCourseId()).setParameter("sid",tc.getSemesterId()).getResultList();
            String cc="",cn="",sn="";
            if (!rows.isEmpty()) { Object[] r=rows.get(0); cc=(String)r[0]; cn=(String)r[1]; sn=(String)r[2]; }
            result.add(TeacherCourseDto.builder().id(tc.getId()).courseId(tc.getCourseId())
                    .courseCode(cc).courseName(cn).semesterId(tc.getSemesterId())
                    .semesterName(sn).className(tc.getClassName()).build());
        }
        return result;
    }

    @Override @Transactional
    public TeacherCourseDto assignTeacher(Long userId, Long courseId, Long semesterId, String className) {
        if (!isAdminOrLv5()) throw new ForbiddenException("Chỉ Admin hoặc Staff lv5 mới có quyền phân công");
        TeacherCourseEntity saved = teacherCourseRepository.save(TeacherCourseEntity.builder()
                .userId(userId).courseId(courseId).semesterId(semesterId).className(className)
                .createdAt(LocalDateTime.now()).build());
        return TeacherCourseDto.builder().id(saved.getId()).courseId(courseId)
                .semesterId(semesterId).className(className).build();
    }

    @Override @Transactional
    public void removeTeacherCourse(Long id) {
        if (!isAdminOrLv5()) throw new ForbiddenException("Chỉ Admin hoặc Staff lv5 mới có quyền này");
        teacherCourseRepository.deleteById(id);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════════════════════════

    private List<CourseGradeDto> mapToGradeDto(List<Object[]> rows) {
        List<CourseGradeDto> result = new ArrayList<>();
        for (Object[] r : rows) {
            result.add(CourseGradeDto.builder()
                    .enrollmentId(((Number)r[0]).longValue()).courseCode((String)r[1]).courseName((String)r[2])
                    .credits(r[3]!=null?((Number)r[3]).intValue():null)
                    .midtermScore(r[4]!=null?((Number)r[4]).doubleValue():null)
                    .finalScore(r[5]!=null?((Number)r[5]).doubleValue():null)
                    .assignmentScore(r[6]!=null?((Number)r[6]).doubleValue():null)
                    .totalScore(r[7]!=null?((Number)r[7]).doubleValue():null)
                    .letterGrade((String)r[8]).gradePoint(r[9]!=null?((Number)r[9]).doubleValue():null)
                    .isPassed(r[10]!=null?(Boolean)r[10]:null).status((String)r[11])
                    .semesterId(r[12]!=null?((Number)r[12]).longValue():null)
                    .semesterName((String)r[13]).academicYear((String)r[14]).build());
        }
        return result;
    }

    private List<ScholarshipDto> mapScholarship(List<Object[]> rows) {
        List<ScholarshipDto> result = new ArrayList<>();
        for (Object[] r : rows) {
            String status = (String)r[7]; // pending_status
            boolean approved = "approved".equals(status) || "received".equals(status);
            result.add(ScholarshipDto.builder()
                    .scholarshipId(((Number)r[0]).longValue()).name((String)r[1]).organization((String)r[2])
                    .amount(r[3]!=null?((Number)r[3]).longValue():null).unit((String)r[4])
                    .minGpa(r[5]!=null?((Number)r[5]).doubleValue():null).description((String)r[6])
                    .status(status).receivedAt(r[8]!=null?r[8].toString():null).approved(approved).build());
        }
        return result;
    }

    /** On-the-fly: nếu approved_at > 6 tháng → EXPIRED */
    private AcademicWarningDto mapWarningToDto(AcademicWarningEntity w) {
        String status = w.getStatus();
        if ("ACTIVE".equals(status) && w.getApprovedAt()!=null
                && w.getApprovedAt().isBefore(LocalDateTime.now().minusMonths(6))) {
            status = "EXPIRED";
        }
        boolean approved = w.getApprovedAt() != null;
        return AcademicWarningDto.builder()
                .warningId(w.getWarningId()).semesterId(w.getSemesterId())
                .warningType(w.getWarningType()).description(w.getDescription())
                .issuedAt(w.getIssuedAt()!=null?w.getIssuedAt().toString():null)
                .resolvedAt(w.getResolvedAt()!=null?w.getResolvedAt().toString():null)
                .status(status).approved(approved)
                .approvedAt(w.getApprovedAt()!=null?w.getApprovedAt().toString():null).build();
    }

    private String buildInitials(String fullName) {
        if (fullName==null||fullName.isBlank()) return "??";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length==1) return parts[0].length()>=2?parts[0].substring(0,2).toUpperCase():parts[0].toUpperCase();
        return (String.valueOf(parts[parts.length-2].charAt(0))+parts[parts.length-1].charAt(0)).toUpperCase();
    }

    private String calcLetterGrade(double t) {
        if(t>=9.0) return "A+"; if(t>=8.5) return "A"; if(t>=8.0) return "B+";
        if(t>=7.0) return "B";  if(t>=6.5) return "C+"; if(t>=5.5) return "C";
        if(t>=5.0) return "D+"; if(t>=4.0) return "D"; return "F";
    }

    private double calcGradePoint(double t) {
        if(t>=8.5) return 4.0; if(t>=8.0) return 3.5; if(t>=7.0) return 3.0;
        if(t>=6.5) return 2.5; if(t>=5.5) return 2.0; if(t>=5.0) return 1.5;
        if(t>=4.0) return 1.0; return 0.0;
    }

    @Override
    public String exportScholarships(String studentCode) {
        Long targetUserId = (studentCode != null && !studentCode.isBlank()) ? resolveUserIdByCode(null, studentCode) : null;
        List<ScholarshipDto> list = getScholarships(targetUserId);
        StringBuilder csv = new StringBuilder("Scholarship ID,Name,Organization,Amount,Unit,Min GPA,Status,Received At,Approved\n");
        for (ScholarshipDto s : list) {
            csv.append(s.getScholarshipId()).append(",")
               .append(escapeCsv(s.getName())).append(",")
               .append(escapeCsv(s.getOrganization())).append(",")
               .append(s.getAmount()).append(",")
               .append(escapeCsv(s.getUnit())).append(",")
               .append(s.getMinGpa()).append(",")
               .append(escapeCsv(s.getStatus())).append(",")
               .append(escapeCsv(s.getReceivedAt())).append(",")
               .append(s.getApproved()).append("\n");
        }
        return csv.toString();
    }

    @Override
    public String exportWarnings(String studentCode, Long semesterId) {
        Long targetUserId = (studentCode != null && !studentCode.isBlank()) ? resolveUserIdByCode(null, studentCode) : null;
        List<AcademicWarningDto> list = getWarnings(targetUserId, semesterId);
        StringBuilder csv = new StringBuilder("Warning ID,Type,Description,Semester ID,Status,Issued At,Resolved At,Approved At\n");
        for (AcademicWarningDto w : list) {
            csv.append(w.getWarningId()).append(",")
               .append(escapeCsv(w.getWarningType())).append(",")
               .append(escapeCsv(w.getDescription())).append(",")
               .append(w.getSemesterId()).append(",")
               .append(escapeCsv(w.getStatus())).append(",")
               .append(escapeCsv(w.getIssuedAt())).append(",")
               .append(escapeCsv(w.getResolvedAt())).append(",")
               .append(escapeCsv(w.getApprovedAt())).append("\n");
        }
        return csv.toString();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        String s = val.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}
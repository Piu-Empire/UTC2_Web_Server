package com.utc2.appreborn.backend.modules.enrollment.service.impl;

import com.utc2.appreborn.backend.exception.BadRequestException;
import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.academic.entity.EnrollmentEntity;
import com.utc2.appreborn.backend.modules.auth.repository.UserRepository;
import com.utc2.appreborn.backend.modules.enrollment.dto.CourseItemDto;
import com.utc2.appreborn.backend.modules.enrollment.dto.EnrollRequest;
import com.utc2.appreborn.backend.modules.enrollment.dto.EnrollmentItemDto;
import com.utc2.appreborn.backend.modules.enrollment.repository.CourseEnrollmentRepository;
import com.utc2.appreborn.backend.modules.enrollment.service.EnrollmentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final int MAX_CREDITS = 24;

    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final UserRepository             userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // ── Helper: lấy userId từ JWT ─────────────────────────────────────────────

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  1. DANH SÁCH MÔN ĐÃ ĐĂNG KÝ + ĐIỂM
    // ════════════════════════════════════════════════════════════════════════════

    @Override
    public List<EnrollmentItemDto> getMyEnrollments() {
        Long userId = currentUserId();
        return courseEnrollmentRepository.findEnrollmentsByUserId(userId)
                .stream()
                .map(this::mapToEnrollmentDto)
                .toList();
    }

    private EnrollmentItemDto mapToEnrollmentDto(Object[] row) {
        // 0=enrollment_id, 1=course_code, 2=course_name, 3=credits,
        // 4=semester_name, 5=status, 6=midterm_score, 7=final_score,
        // 8=assignment_score, 9=total_score, 10=letter_grade, 11=grade_point,
        // 12=is_passed, 13=registered_at
        return EnrollmentItemDto.builder()
                .enrollmentId(((Number) row[0]).longValue())
                .courseCode((String) row[1])
                .courseName((String) row[2])
                .credits(row[3] != null ? ((Number) row[3]).intValue() : null)
                .semesterName((String) row[4])
                .status((String) row[5])
                .midtermScore(row[6] != null ? ((Number) row[6]).doubleValue() : null)
                .finalScore(row[7] != null ? ((Number) row[7]).doubleValue() : null)
                .assignmentScore(row[8] != null ? ((Number) row[8]).doubleValue() : null)
                .totalScore(row[9] != null ? ((Number) row[9]).doubleValue() : null)
                .letterGrade((String) row[10])
                .gradePoint(row[11] != null ? ((Number) row[11]).doubleValue() : null)
                .isPassed(row[12] != null ? ((Number) row[12]).intValue() == 1 : null)
                .registeredAt(row[13] != null ? row[13].toString() : null)
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  2. DANH SÁCH MÔN CÓ THỂ ĐĂNG KÝ
    // ════════════════════════════════════════════════════════════════════════════

    @Override
    public List<CourseItemDto> getAvailableCourses() {
        Long userId = currentUserId();

        List<Object[]> rows = entityManager.createNativeQuery(
                "SELECT course_id, course_code, course_name, credits, " +
                        "theory_hours, practice_hours, department, description FROM course ORDER BY course_code ASC"
        ).getResultList();

        return rows.stream()
                .map(row -> {
                    Long courseId = ((Number) row[0]).longValue();
                    boolean enrolled = courseEnrollmentRepository
                            .existsByUserIdAndCourseIdAndStatusNot(userId, courseId, "đã hủy");
                    return mapToCourseDto(row, enrolled);
                })
                .toList();
    }

    private CourseItemDto mapToCourseDto(Object[] row, boolean alreadyEnrolled) {
        // 0=course_id, 1=course_code, 2=course_name, 3=credits,
        // 4=theory_hours, 5=practice_hours, 6=department, 7=description
        return CourseItemDto.builder()
                .courseId(((Number) row[0]).longValue())
                .courseCode((String) row[1])
                .courseName((String) row[2])
                .credits(row[3] != null ? ((Number) row[3]).intValue() : null)
                .theoryHours(row[4] != null ? ((Number) row[4]).intValue() : null)
                .practiceHours(row[5] != null ? ((Number) row[5]).intValue() : null)
                .department((String) row[6])
                .description((String) row[7])
                .alreadyEnrolled(alreadyEnrolled)
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  3. ĐĂNG KÝ MÔN HỌC
    // ════════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public EnrollmentItemDto enroll(EnrollRequest request) {
        Long userId = currentUserId();

        Object[] course = getCourseById(request.getCourseId());
        String semesterName = getSemesterName(request.getSemesterId());

        if (courseEnrollmentRepository.existsByUserIdAndCourseIdAndStatusNot(userId, request.getCourseId(), "đã hủy")) {
            throw new BadRequestException("Bạn đã đăng ký môn \"" + course[2] + "\" rồi!");
        }

        int currentCredits = courseEnrollmentRepository
                .sumCreditsByUserIdAndSemesterId(userId, request.getSemesterId());
        int courseCredits = ((Number) course[3]).intValue();
        if (currentCredits + courseCredits > MAX_CREDITS) {
            throw new BadRequestException(
                    "Vượt quá số tín chỉ tối đa (" + MAX_CREDITS + " tín chỉ/học kỳ)!");
        }

        EnrollmentEntity saved = courseEnrollmentRepository.save(
                EnrollmentEntity.builder()
                        .userId(userId)
                        .courseId(request.getCourseId())
                        .semesterId(request.getSemesterId())
                        .status("đã đăng ký")
                        .build()
        );

        return EnrollmentItemDto.builder()
                .enrollmentId(saved.getEnrollmentId())
                .courseCode((String) course[1])
                .courseName((String) course[2])
                .credits(courseCredits)
                .semesterName(semesterName)
                .status(saved.getStatus())
                .registeredAt(saved.getRegisteredAt() != null ? saved.getRegisteredAt().toString() : null)
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  4. HỦY ĐĂNG KÝ MÔN HỌC
    // ════════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void cancelEnrollment(Long enrollmentId) {
        Long userId = currentUserId();

        EnrollmentEntity enrollment = courseEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký"));

        if (!enrollment.getUserId().equals(userId)) {
            throw new BadRequestException("Không có quyền hủy đăng ký này");
        }

        if ("đã hủy".equals(enrollment.getStatus())) {
            throw new BadRequestException("Đăng ký này đã bị hủy rồi");
        }

        if ("hoàn thành".equals(enrollment.getStatus())) {
            throw new BadRequestException("Không thể hủy môn học đã hoàn thành");
        }

        enrollment.setStatus("đã hủy");
        courseEnrollmentRepository.save(enrollment);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Object[] getCourseById(Long courseId) {
        try {
            return (Object[]) entityManager
                    .createNativeQuery(
                            "SELECT course_id, course_code, course_name, credits FROM course WHERE course_id = :id")
                    .setParameter("id", courseId)
                    .getSingleResult();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Môn học không tồn tại");
        }
    }

    private String getSemesterName(Long semesterId) {
        try {
            Object result = entityManager
                    .createNativeQuery("SELECT semester_name FROM semester WHERE semester_id = :id")
                    .setParameter("id", semesterId)
                    .getSingleResult();
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            throw new ResourceNotFoundException("Học kỳ không tồn tại");
        }
    }
}

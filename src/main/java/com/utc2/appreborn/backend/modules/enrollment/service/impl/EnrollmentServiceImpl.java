package com.utc2.appreborn.backend.modules.enrollment.service.impl;

import com.utc2.appreborn.backend.exception.BadRequestException;
import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.enrollment.entity.EnrollmentEntity;
import com.utc2.appreborn.backend.modules.auth.entity.User;
import com.utc2.appreborn.backend.modules.auth.repository.UserRepository;
import com.utc2.appreborn.backend.modules.enrollment.dto.CourseItemDto;
import com.utc2.appreborn.backend.modules.enrollment.dto.EnrollRequest;
import com.utc2.appreborn.backend.modules.enrollment.dto.EnrollmentItemDto;
import com.utc2.appreborn.backend.modules.enrollment.repository.CourseEnrollmentRepository;
import com.utc2.appreborn.backend.modules.enrollment.service.EnrollmentService;
import com.utc2.appreborn.backend.modules.finance.entity.TuitionFee;
import com.utc2.appreborn.backend.modules.finance.repository.TuitionFeeRepository;
import com.utc2.appreborn.backend.modules.finance.repository.TuitionRateRepository;
import com.utc2.appreborn.backend.modules.academic.repository.SemesterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final int    MAX_CREDITS       = 24;
    private static final String FEE_TYPE_SUBJECT  = "SUBJECT";
    private static final String FEE_STATUS_UNPAID = "chưa đóng";

    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final UserRepository             userRepository;
    private final TuitionFeeRepository       tuitionFeeRepository;
    private final TuitionRateRepository      tuitionRateRepository;
    private final SemesterRepository         semesterRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // ── Helper: lấy User hiện tại từ SecurityContext ──────────────────────────
    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Long currentUserId() {
        return currentUser().getId();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 1. DANH SÁCH MÔN ĐÃ ĐĂNG KÝ + ĐIỂM
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
        // 12=is_passed, 13=registered_at, 14=semester_number, 15=academic_year
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
                .isPassed(row[12] != null ? (Boolean) row[12] : null)
                .registeredAt(row[13] != null ? row[13].toString() : null)
                .semesterNumber(row[14] != null ? ((Number) row[14]).intValue() : 0)
                .academicYear((String) row[15])
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 2. DANH SÁCH MÔN CÓ THỂ ĐĂNG KÝ
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
    // 3. ĐĂNG KÝ MÔN HỌC (có tạo/cập nhật học phí)
    // ════════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public EnrollmentItemDto enroll(EnrollRequest request) {
        User user = currentUser();
        Long userId = user.getId();

        Object[] course = getCourseById(request.getCourseId());
        String semesterName = getSemesterNameForUser(userId, request.getSemesterId()); // có kiểm tra user_id

        // Kiểm tra đã đăng ký chưa
        if (courseEnrollmentRepository.existsByUserIdAndCourseIdAndStatusNot(userId, request.getCourseId(), "đã hủy")) {
            throw new BadRequestException("Bạn đã đăng ký môn \"" + course[2] + "\" rồi!");
        }

        // Kiểm tra tín chỉ
        Integer rawCredits = courseEnrollmentRepository
                .sumCreditsByUserIdAndSemesterId(userId, request.getSemesterId());
        int currentCredits = (rawCredits != null) ? rawCredits : 0;
        int courseCredits = ((Number) course[3]).intValue();
        if (currentCredits + courseCredits > MAX_CREDITS) {
            throw new BadRequestException(
                    "Vượt quá số tín chỉ tối đa (" + MAX_CREDITS + " tín chỉ/học kỳ)!");
        }

        // Lưu enrollment
        EnrollmentEntity saved = courseEnrollmentRepository.save(
                EnrollmentEntity.builder()
                        .userId(userId)
                        .courseId(request.getCourseId())
                        .semesterId(request.getSemesterId())
                        .status("đã đăng ký")
                        .build()
        );

        // ── Cập nhật học phí ──────────────────────────────────────────────
        Integer newTotalCredits = courseEnrollmentRepository
                .sumCreditsByUserIdAndSemesterId(userId, request.getSemesterId());
        if (newTotalCredits == null) newTotalCredits = courseCredits;

        String academicYear = semesterRepository.findById(request.getSemesterId())
                .map(s -> s.getAcademicYear())
                .orElse(null);
        BigDecimal pricePerCredit = tuitionRateRepository
                .findByAcademicYearOrDefault(academicYear)
                .map(r -> r.getPricePerCredit())
                .orElse(BigDecimal.valueOf(550_000));

        BigDecimal newTotal = BigDecimal.valueOf(newTotalCredits).multiply(pricePerCredit);
        LocalDate dueDate = LocalDate.now().plusMonths(1).withDayOfMonth(15);

        tuitionFeeRepository.findFirstByUserIdAndSemesterIdAndFeeType(userId, request.getSemesterId(), FEE_TYPE_SUBJECT)
                .ifPresentOrElse(
                        existingFee -> {
                            if ("đã đóng đủ".equals(existingFee.getStatus())) {
                                // Đã đóng đủ trước đó → tạo fee mới cho phần tăng thêm
                                BigDecimal alreadyPaid = existingFee.getTotalAmount() != null
                                        ? existingFee.getTotalAmount() : BigDecimal.ZERO;
                                BigDecimal extraAmount = newTotal.subtract(alreadyPaid);
                                if (extraAmount.compareTo(BigDecimal.ZERO) > 0) {
                                    tuitionFeeRepository.save(TuitionFee.builder()
                                            .user(user)
                                            .semesterId(request.getSemesterId())
                                            .feeType(FEE_TYPE_SUBJECT)
                                            .totalAmount(extraAmount)
                                            .paidAmount(BigDecimal.ZERO)
                                            .remainingAmount(extraAmount)
                                            .dueDate(dueDate)
                                            .status(FEE_STATUS_UNPAID)
                                            .build());
                                }
                            } else {
                                // Chưa đóng đủ → cập nhật tổng tiền
                                existingFee.setTotalAmount(newTotal);
                                existingFee.setRemainingAmount(newTotal.subtract(
                                        existingFee.getPaidAmount() != null ? existingFee.getPaidAmount() : BigDecimal.ZERO));
                                tuitionFeeRepository.save(existingFee);
                            }
                        },
                        () -> {
                            // Chưa có fee → tạo mới
                            tuitionFeeRepository.save(TuitionFee.builder()
                                    .user(user)
                                    .semesterId(request.getSemesterId())
                                    .feeType(FEE_TYPE_SUBJECT)
                                    .totalAmount(newTotal)
                                    .paidAmount(BigDecimal.ZERO)
                                    .remainingAmount(newTotal)
                                    .dueDate(dueDate)
                                    .status(FEE_STATUS_UNPAID)
                                    .build());
                        }
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
    // 4. HỦY ĐĂNG KÝ MÔN HỌC (có cập nhật học phí)
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

        // ── Cập nhật lại fee khi hủy môn ────────────────────────────────────
        Integer remainingCredits = courseEnrollmentRepository
                .sumCreditsByUserIdAndSemesterId(userId, enrollment.getSemesterId());

        if (remainingCredits == null || remainingCredits == 0) {
            // Không còn môn nào trong kỳ → xóa fee SUBJECT nếu chưa đóng
            tuitionFeeRepository.findFirstByUserIdAndSemesterIdAndFeeType(userId, enrollment.getSemesterId(), FEE_TYPE_SUBJECT)
                    .ifPresent(fee -> {
                        if (!"đã đóng đủ".equals(fee.getStatus())) {
                            tuitionFeeRepository.delete(fee);
                        }
                    });
        } else {
            // Còn môn → tính lại totalAmount
            String academicYear = semesterRepository.findById(enrollment.getSemesterId())
                    .map(s -> s.getAcademicYear())
                    .orElse(null);
            BigDecimal pricePerCredit = tuitionRateRepository
                    .findByAcademicYearOrDefault(academicYear)
                    .map(r -> r.getPricePerCredit())
                    .orElse(BigDecimal.valueOf(550_000));

            BigDecimal newTotal = BigDecimal.valueOf(remainingCredits).multiply(pricePerCredit);
            tuitionFeeRepository.findFirstByUserIdAndSemesterIdAndFeeType(userId, enrollment.getSemesterId(), FEE_TYPE_SUBJECT)
                    .ifPresent(fee -> {
                        if (!"đã đóng đủ".equals(fee.getStatus())) {
                            fee.setTotalAmount(newTotal);
                            fee.setRemainingAmount(newTotal.subtract(
                                    fee.getPaidAmount() != null ? fee.getPaidAmount() : BigDecimal.ZERO));
                            tuitionFeeRepository.save(fee);
                        }
                    });
        }
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

    /**
     * Lấy tên học kỳ, đồng thời kiểm tra học kỳ đó có thuộc về user hiện tại không.
     * Tránh trường hợp sinh viên đăng ký môn vào học kỳ của người khác.
     */
    private String getSemesterNameForUser(Long userId, Long semesterId) {
        try {
            Object result = entityManager
                    .createNativeQuery(
                            "SELECT semester_name FROM semester WHERE semester_id = :id AND user_id = :userId")
                    .setParameter("id", semesterId)
                    .setParameter("userId", userId)
                    .getSingleResult();
            if (result == null)
                throw new BadRequestException("Học kỳ không thuộc về tài khoản này");
            return result.toString();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Học kỳ không tồn tại hoặc không thuộc về bạn");
        }
    }
}
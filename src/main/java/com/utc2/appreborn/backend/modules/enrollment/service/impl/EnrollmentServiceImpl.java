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
import com.utc2.appreborn.backend.modules.notification.service.NotificationService;
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
    private static final String FEE_STATUS_PAID   = "đã đóng đủ";

    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final UserRepository             userRepository;
    private final TuitionFeeRepository       tuitionFeeRepository;
    private final TuitionRateRepository      tuitionRateRepository;
    private final SemesterRepository         semesterRepository;
    private final NotificationService        notificationService;

    @PersistenceContext
    private EntityManager entityManager;

    // ── Helpers: lấy User entity / userId từ JWT ─────────────────────────────

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Long currentUserId() {
        return currentUser().getId();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  1. DANH SÁCH MÔN ĐÃ ĐĂNG KÝ + ĐIỂM
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public List<EnrollmentItemDto> getMyEnrollments() {
        Long userId = currentUserId();
        return courseEnrollmentRepository.findEnrollmentsByUserId(userId)
                .stream()
                .map(this::mapToEnrollmentDto)
                .toList();
    }

    private EnrollmentItemDto mapToEnrollmentDto(Object[] row) {
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

    // ═════════════════════════════════════════════════════════════════════════
    //  2. DANH SÁCH MÔN CÓ THỂ ĐĂNG KÝ
    // ═════════════════════════════════════════════════════════════════════════

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

    // ═════════════════════════════════════════════════════════════════════════
    //  3. ĐĂNG KÝ MÔN HỌC
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public EnrollmentItemDto enroll(EnrollRequest request) {
        User user = currentUser();
        Long userId = user.getId();

        Object[] course = getCourseById(request.getCourseId());
        String semesterName = getSemesterNameForUser(userId, request.getSemesterId());

        if (courseEnrollmentRepository.existsByUserIdAndCourseIdAndStatusNot(userId, request.getCourseId(), "đã hủy")) {
            throw new BadRequestException("Bạn đã đăng ký môn \"" + course[2] + "\" rồi!");
        }

        Integer rawCredits = courseEnrollmentRepository
                .sumCreditsByUserIdAndSemesterId(userId, request.getSemesterId());
        int currentCredits = (rawCredits != null) ? rawCredits : 0;
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

        // ── Tạo/cập nhật fee record cho học kỳ này ───────────────────────────
        // Tính lại tổng tín chỉ (bao gồm môn vừa đăng ký)
        Integer newTotalCredits = courseEnrollmentRepository
                .sumCreditsByUserIdAndSemesterId(userId, request.getSemesterId());
        if (newTotalCredits == null) newTotalCredits = courseCredits;

        String academicYear = semesterRepository.findById(request.getSemesterId())
                .map(s -> s.getAcademicYear())
                .orElse(null);
        BigDecimal pricePerCredit = tuitionRateRepository
                .findByAcademicYearOrDefault(academicYear)
                .map(r -> r.getPricePerCredit())
                .orElse(BigDecimal.valueOf(600_000));

        BigDecimal newTotal = BigDecimal.valueOf(newTotalCredits).multiply(pricePerCredit);
        LocalDate dueDate   = LocalDate.now().plusMonths(1).withDayOfMonth(15);

        // FIX: dùng findFirstByUserIdAndSemesterIdAndFeeTypeAndStatusNot để
        // tìm đúng record "chưa đóng" — tránh đụng vào record đã đóng đủ.
        tuitionFeeRepository
                .findFirstByUserIdAndSemesterIdAndFeeTypeAndStatusNot(
                        userId, request.getSemesterId(), FEE_TYPE_SUBJECT, FEE_STATUS_PAID)
                .ifPresentOrElse(
                        existingFee -> {
                            // Fee chưa đóng đủ → cập nhật lại tổng tiền
                            existingFee.setTotalAmount(newTotal);
                            existingFee.setRemainingAmount(newTotal.subtract(
                                    existingFee.getPaidAmount() != null
                                            ? existingFee.getPaidAmount()
                                            : BigDecimal.ZERO));
                            tuitionFeeRepository.save(existingFee);
                        },
                        () -> {
                            // Không có fee chưa đóng → kiểm tra xem có fee đã đóng đủ không
                            List<TuitionFee> paidFees = tuitionFeeRepository
                                    .findAllByUserIdAndSemesterIdAndFeeType(
                                            userId, request.getSemesterId(), FEE_TYPE_SUBJECT);

                            boolean hasPaidFee = paidFees.stream()
                                    .anyMatch(f -> FEE_STATUS_PAID.equals(f.getStatus()));

                            if (hasPaidFee) {
                                // Đã đóng tiền trước, đăng ký thêm môn → tạo fee phụ cho phần tăng thêm
                                BigDecimal alreadyPaidTotal = paidFees.stream()
                                        .filter(f -> FEE_STATUS_PAID.equals(f.getStatus()))
                                        .map(TuitionFee::getTotalAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                BigDecimal extraAmount = newTotal.subtract(alreadyPaidTotal);
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
                                // Chưa có fee nào → tạo mới
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
                        }
                );

        EnrollmentItemDto result = EnrollmentItemDto.builder()
                .enrollmentId(saved.getEnrollmentId())
                .courseCode((String) course[1])
                .courseName((String) course[2])
                .credits(courseCredits)
                .semesterName(semesterName)
                .status(saved.getStatus())
                .registeredAt(saved.getRegisteredAt() != null ? saved.getRegisteredAt().toString() : null)
                .build();
                
        // Push notification
        try {
            notificationService.createSystemNotification(
                    userId,
                    "COURSE_ENROLLED",
                    "Đăng ký môn học thành công",
                    "Bạn đã đăng ký thành công môn " + course[2] + " (" + course[1] + ").",
                    "ENROLLMENT",
                    saved.getEnrollmentId()
            );
        } catch (Exception ignored) {}
        
        return result;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  4. HỦY ĐĂNG KÝ MÔN HỌC
    // ═════════════════════════════════════════════════════════════════════════

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

        // ── Cập nhật lại fee khi hủy môn ─────────────────────────────────────
        Integer credits = courseEnrollmentRepository
                .sumCreditsByUserIdAndSemesterId(userId, enrollment.getSemesterId());
        final int remainingCredits = (credits != null) ? credits : 0;

        String cancelAcademicYear = semesterRepository.findById(enrollment.getSemesterId())
                .map(s -> s.getAcademicYear()).orElse(null);
        BigDecimal cancelPricePerCredit = tuitionRateRepository
                .findByAcademicYearOrDefault(cancelAcademicYear)
                .map(r -> r.getPricePerCredit())
                .orElse(BigDecimal.valueOf(600_000));

        // FIX: chỉ update fee record "chưa đóng đủ" — không đụng vào record đã đóng
        tuitionFeeRepository
                .findFirstByUserIdAndSemesterIdAndFeeTypeAndStatusNot(
                        userId, enrollment.getSemesterId(), FEE_TYPE_SUBJECT, FEE_STATUS_PAID)
                .ifPresent(fee -> {
                    if (remainingCredits == 0) {
                        // Hủy hết môn → xóa fee record chưa đóng
                        tuitionFeeRepository.delete(fee);
                    } else {
                        // Còn môn khác → tính lại số tiền
                        BigDecimal newTotal = BigDecimal.valueOf(remainingCredits)
                                .multiply(cancelPricePerCredit);
                        fee.setTotalAmount(newTotal);
                        fee.setRemainingAmount(newTotal.subtract(
                                fee.getPaidAmount() != null ? fee.getPaidAmount() : BigDecimal.ZERO));
                        tuitionFeeRepository.save(fee);
                    }
                });
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

    private String getSemesterNameForUser(Long userId, Long semesterId) {
        try {
            Object result = entityManager
                    .createNativeQuery(
                            "SELECT semester_name FROM semester WHERE semester_id = :id")
                    .setParameter("id", semesterId)
                    .getSingleResult();
            if (result == null) throw new BadRequestException("Học kỳ không tồn tại");
            return result.toString();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Học kỳ không tồn tại");
        }
    }
}

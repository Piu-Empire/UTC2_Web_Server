package com.utc2.appreborn.backend.modules.finance.service.impl;

import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.auth.entity.User;
import com.utc2.appreborn.backend.modules.finance.dto.TuitionResponse;
import com.utc2.appreborn.backend.modules.finance.dto.TuitionSummaryResponse;
import com.utc2.appreborn.backend.modules.finance.entity.TuitionFee;
import com.utc2.appreborn.backend.modules.finance.repository.TuitionFeeRepository;
import com.utc2.appreborn.backend.modules.finance.service.TuitionService;
import com.utc2.appreborn.backend.modules.profile.entity.StudentProfileEntity;
import com.utc2.appreborn.backend.modules.profile.entity.UserProfileEntity;
import com.utc2.appreborn.backend.modules.profile.repository.StudentProfileRepository;
import com.utc2.appreborn.backend.modules.profile.repository.UserProfileRepository;
import com.utc2.appreborn.backend.modules.academic.repository.SemesterRepository;
import com.utc2.appreborn.backend.modules.enrollment.repository.CourseEnrollmentRepository;
import com.utc2.appreborn.backend.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TuitionServiceImpl implements TuitionService {

    private final TuitionFeeRepository        tuitionFeeRepository;
    private final StudentProfileRepository    studentProfileRepository;
    private final UserProfileRepository       userProfileRepository;
    private final SemesterRepository          semesterRepository;
    private final CourseEnrollmentRepository  enrollmentRepository;
    private final NotificationService         notificationService;

    private static final String STATUS_UNPAID  = "chưa đóng";
    private static final String STATUS_PAID    = "đã đóng đủ";
    private static final String FEE_SUBJECT    = "SUBJECT";
    private static final String FEE_DORMITORY  = "DORMITORY";

    @Override
    public TuitionSummaryResponse getMyTuitionSummary(String username) {
        StudentProfileEntity sp = findStudentByUsername(username);
        User user = sp.getUser();
        UserProfileEntity up = userProfileRepository.findById(user.getId()).orElse(null);
        String fullName = up != null ? up.getFullName() : user.getEmail();

        List<TuitionFee> subjectFees = tuitionFeeRepository
                .findByUserIdAndFeeTypeOrderBySemesterIdDesc(user.getId(), FEE_SUBJECT);
        List<TuitionFee> dormFees = tuitionFeeRepository
                .findByUserIdAndFeeTypeOrderBySemesterIdDesc(user.getId(), FEE_DORMITORY);
        BigDecimal totalDebt = tuitionFeeRepository.sumRemainingByUserId(user.getId());

        return TuitionSummaryResponse.builder()
                .studentId(sp.getStudentCode())
                .fullName(fullName)
                .totalDebt(totalDebt)
                .semesters(subjectFees.stream()
                        .map(f -> toResponse(f, sp, up))
                        .collect(Collectors.toList()))
                .dormitory(dormFees.stream()
                        .map(f -> toResponse(f, sp, up))
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public List<TuitionResponse> getMyTuitionHistory(String username) {
        StudentProfileEntity sp = findStudentByUsername(username);
        UserProfileEntity    up = userProfileRepository.findById(sp.getUser().getId()).orElse(null);
        return tuitionFeeRepository
                .findByUserIdAndFeeTypeOrderBySemesterIdDesc(sp.getUser().getId(), FEE_SUBJECT)
                .stream()
                .map(f -> toResponse(f, sp, up))
                .collect(Collectors.toList());
    }

    @Override
    public List<TuitionResponse> getMyPaidHistory(String username) {
        StudentProfileEntity sp = findStudentByUsername(username);
        UserProfileEntity    up = userProfileRepository.findById(sp.getUser().getId()).orElse(null);
        return tuitionFeeRepository
                .findByUserIdAndFeeTypeAndStatus(sp.getUser().getId(), FEE_SUBJECT, STATUS_PAID)
                .stream()
                .map(f -> toResponse(f, sp, up))
                .collect(Collectors.toList());
    }

    @Override
    public TuitionResponse getTuitionBySemester(String username, String semester) {
        StudentProfileEntity sp = findStudentByUsername(username);
        UserProfileEntity    up = userProfileRepository.findById(sp.getUser().getId()).orElse(null);

        Long semesterId;
        try {
            semesterId = Long.parseLong(semester);
        } catch (NumberFormatException e) {
            throw new ResourceNotFoundException("semester phải là số: " + semester);
        }

        TuitionFee fee = tuitionFeeRepository
                .findFirstUnpaidByUserIdAndSemesterId(sp.getUser().getId(), semesterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không có dữ liệu học phí kỳ: " + semester));

        return toResponse(fee, sp, up);
    }

    @Override
    @Transactional
    public TuitionResponse payTuition(String username, Long semesterId, String paymentMethod) {
        StudentProfileEntity sp = findStudentByUsername(username);

        TuitionFee fee = tuitionFeeRepository
                .findFirstUnpaidByUserIdAndSemesterId(sp.getUser().getId(), semesterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy học phí kỳ: " + semesterId));

        if (STATUS_PAID.equals(fee.getStatus())) {
            throw new IllegalStateException("Học phí kỳ này đã được đóng rồi");
        }

        fee.setPaidAmount(fee.getTotalAmount());
        fee.setRemainingAmount(BigDecimal.ZERO);
        fee.setStatus(STATUS_PAID);
        fee.setPaymentMethod(paymentMethod != null ? paymentMethod : "online");
        fee.setPaidAt(LocalDateTime.now());

        tuitionFeeRepository.save(fee);

        UserProfileEntity up = userProfileRepository.findById(sp.getUser().getId()).orElse(null);
        TuitionResponse result = toResponse(fee, sp, up);
        
        // Push notification
        try {
            notificationService.createSystemNotification(
                    sp.getUser().getId(),
                    "TUITION_PAID",
                    "Thanh toán học phí thành công",
                    "Bạn đã thanh toán thành công " + fee.getPaidAmount() + " cho kỳ " + (result.getSemesterName() != null ? result.getSemesterName() : semesterId),
                    "TUITION",
                    fee.getId()
            );
        } catch (Exception ignored) {}
        
        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private StudentProfileEntity findStudentByUsername(String username) {
        if (username == null || !username.contains("@")) {
            throw new ResourceNotFoundException("Không xác định được tài khoản: " + username);
        }
        String studentCode = username.split("@")[0];
        return studentProfileRepository.findByStudentCodeWithUser(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tài khoản này không phải sinh viên (MSSV: " + studentCode + ")"));
    }

    private TuitionResponse toResponse(TuitionFee fee, StudentProfileEntity sp, UserProfileEntity up) {
        String semName = fee.getSemesterId() != null
                ? semesterRepository.findById(fee.getSemesterId())
                .map(s -> s.getSemesterName()).orElse("Học kỳ " + fee.getSemesterId())
                : null;

        // FIX: tính tổng tín chỉ đăng ký trong kỳ để trả về cho app hiển thị "X TC"
        Integer totalCredits = 0;
        if (fee.getSemesterId() != null && fee.getUserId() != null) {
            totalCredits = enrollmentRepository.sumCreditsByUserIdAndSemesterId(
                    fee.getUserId(), fee.getSemesterId());
            if (totalCredits == null) totalCredits = 0;
        }

        return TuitionResponse.builder()
                .id(fee.getId())
                .studentId(sp.getStudentCode())
                .fullName(up != null ? up.getFullName() : null)
                .semesterId(fee.getSemesterId())
                .semesterName(semName)
                .totalAmount(fee.getTotalAmount())
                .paidAmount(fee.getPaidAmount())
                .remainingAmount(fee.getRemainingAmount())
                .totalCredits(totalCredits)
                .dueDate(fee.getDueDate())
                .paidAt(fee.getPaidAt())
                .status(fee.getStatus())
                .paymentMethod(fee.getPaymentMethod())
                .build();
    }
}

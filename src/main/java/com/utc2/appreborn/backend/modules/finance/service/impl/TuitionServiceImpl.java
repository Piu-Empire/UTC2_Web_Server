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

    private final TuitionFeeRepository       tuitionFeeRepository;
    private final StudentProfileRepository   studentProfileRepository;
    private final UserProfileRepository      userProfileRepository;
    private final SemesterRepository         semesterRepository;
    // ADD: để tính tổng tín chỉ trả về cho Android hiển thị "X TC"
    private final CourseEnrollmentRepository courseEnrollmentRepository;

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

        // FIX: dùng findFirstUnpaidByUserIdAndSemesterId để ưu tiên record chưa đóng
        // khi có nhiều fee records trên cùng 1 kỳ (đăng ký thêm môn sau khi đã đóng tiền)
        TuitionFee fee = tuitionFeeRepository
                .findFirstByUserIdAndSemesterIdAndFeeType(sp.getUser().getId(), semesterId, FEE_SUBJECT)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không có dữ liệu học phí kỳ: " + semester));

        return toResponse(fee, sp, up);
    }

    /**
     * Thanh toán học phí còn lại của 1 kỳ.
     * FIX: dùng findFirstUnpaidByUserIdAndSemesterId thay vì findByUserIdAndSemesterId
     * để đảm bảo đóng đúng record "chưa đóng" khi có nhiều records cùng kỳ.
     */
    @Override
    @Transactional
    public TuitionResponse payTuition(String username, Long semesterId, String paymentMethod) {
        StudentProfileEntity sp = findStudentByUsername(username);

        // FIX: ưu tiên lấy record chưa đóng đủ
        TuitionFee fee = tuitionFeeRepository
                .findFirstByUserIdAndSemesterIdAndFeeType(sp.getUser().getId(), semesterId, FEE_SUBJECT)
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
        return toResponse(fee, sp, up);
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

        // ADD: tính tổng tín chỉ để Android hiển thị "X TC"
        // Chỉ tính cho fee SUBJECT có semesterId, fee DORMITORY để null
        Integer totalCredits = null;
        if (fee.getSemesterId() != null && FEE_SUBJECT.equals(fee.getFeeType())) {
            totalCredits = courseEnrollmentRepository
                    .sumCreditsByUserIdAndSemesterId(sp.getUser().getId(), fee.getSemesterId());
        }

        return TuitionResponse.builder()
                .id(fee.getId())
                .studentId(sp.getStudentCode())
                .fullName(up != null ? up.getFullName() : null)
                .semesterId(fee.getSemesterId())
                .semesterName(semName)
                .dormRegId(fee.getDormRegId())
                .totalCredits(totalCredits)
                .totalAmount(fee.getTotalAmount())
                .paidAmount(fee.getPaidAmount())
                .remainingAmount(fee.getRemainingAmount())
                .dueDate(fee.getDueDate())
                .paidAt(fee.getPaidAt())
                .status(fee.getStatus())
                .paymentMethod(fee.getPaymentMethod())
                .build();
    }
}
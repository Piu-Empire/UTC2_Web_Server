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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TuitionServiceImpl implements TuitionService {

    private final TuitionFeeRepository     tuitionFeeRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserProfileRepository    userProfileRepository;

    @Override
    public TuitionSummaryResponse getMyTuitionSummary(String username) {
        StudentProfileEntity sp = findStudentByUsername(username);
        User user = sp.getUser();

        // FIX: findById thay vì findByUserId (userId là @Id của UserProfile)
        UserProfileEntity up = userProfileRepository.findById(user.getId()).orElse(null);
        String fullName = up != null ? up.getFullName() : user.getEmail();

        List<TuitionFee> fees = tuitionFeeRepository.findByUserIdOrderBySemesterIdDesc(user.getId());
        BigDecimal totalDebt  = tuitionFeeRepository.sumRemainingByUserId(user.getId());

        return TuitionSummaryResponse.builder()
                .studentId(sp.getStudentCode())
                .fullName(fullName)
                .totalDebt(totalDebt)
                .semesters(fees.stream()
                        .map(f -> toResponse(f, sp, up))
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public List<TuitionResponse> getMyTuitionHistory(String username) {
        StudentProfileEntity sp = findStudentByUsername(username);

        // FIX: findById
        UserProfileEntity up = userProfileRepository.findById(sp.getUser().getId()).orElse(null);

        return tuitionFeeRepository
                .findByUserIdOrderBySemesterIdDesc(sp.getUser().getId())
                .stream()
                .map(f -> toResponse(f, sp, up))
                .collect(Collectors.toList());
    }

    @Override
    public TuitionResponse getTuitionBySemester(String username, String semester) {
        StudentProfileEntity sp = findStudentByUsername(username);

        // FIX: findById
        UserProfileEntity up = userProfileRepository.findById(sp.getUser().getId()).orElse(null);

        Long semesterId;
        try {
            semesterId = Long.parseLong(semester);
        } catch (NumberFormatException e) {
            throw new ResourceNotFoundException("semester phải là số: " + semester);
        }

        TuitionFee fee = tuitionFeeRepository
                .findByUserIdAndSemesterId(sp.getUser().getId(), semesterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không có dữ liệu học phí kỳ: " + semester));

        return toResponse(fee, sp, up);
    }

    // ── Helpers ───────────────────────────────────────────────

    /**
     * FIX WARN 1: username từ JWT = email → extract studentCode trước khi tìm
     */
    private StudentProfileEntity findStudentByUsername(String username) {
        String studentCode = username.contains("@") ? username.split("@")[0] : username;
        return studentProfileRepository.findByStudentCodeWithUser(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sinh viên MSSV: " + studentCode));
    }

    private TuitionResponse toResponse(TuitionFee fee, StudentProfileEntity sp, UserProfileEntity up) {
        return TuitionResponse.builder()
                .id(fee.getId())
                .studentId(sp.getStudentCode())
                .fullName(up != null ? up.getFullName() : null)
                .semesterId(fee.getSemesterId())
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
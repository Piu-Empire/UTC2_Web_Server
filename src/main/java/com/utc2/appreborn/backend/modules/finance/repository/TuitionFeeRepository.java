package com.utc2.appreborn.backend.modules.finance.repository;

import com.utc2.appreborn.backend.modules.finance.entity.TuitionFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TuitionFeeRepository extends JpaRepository<TuitionFee, Long> {

    List<TuitionFee> findByUserIdOrderBySemesterIdDesc(Long userId);

    List<TuitionFee> findByUserIdAndFeeTypeOrderBySemesterIdDesc(Long userId, String feeType);

    List<TuitionFee> findByUserIdAndFeeTypeAndStatus(Long userId, String feeType, String status);

    Optional<TuitionFee> findByDormRegId(Long dormRegId);

    void deleteByDormRegIdAndStatus(Long dormRegId, String status);

    // Dùng cho TuitionServiceImpl (query theo semesterId, không filter feeType)
    Optional<TuitionFee> findByUserIdAndSemesterId(Long userId, Long semesterId);

    /**
     * FIX CHÍNH: Dùng thay findByUserIdAndSemesterId trong EnrollmentServiceImpl.
     *
     * Vấn đề: findByUserIdAndSemesterId không filter feeType → khi cùng (userId, semesterId)
     * có cả fee SUBJECT + DORMITORY, hoặc có nhiều fee SUBJECT (1 đã đóng + 1 chưa đóng),
     * Spring ném IncorrectResultSizeDataAccessException → transaction rollback → fee không
     * được tạo/update → màn hình thanh toán luôn hiện 0đ.
     *
     * findFirst đảm bảo luôn trả đúng 1 record dù có bao nhiêu row trong DB.
     */
    Optional<TuitionFee> findFirstByUserIdAndSemesterIdAndFeeType(
            Long userId, Long semesterId, String feeType);

    /**
     * Alias cho TuitionServiceImpl.findFirstUnpaidByUserIdAndSemesterId()
     * (fix build error: method này được gọi ở dòng 104 và 123 của TuitionServiceImpl).
     */
    Optional<TuitionFee> findFirstByUserIdAndSemesterIdAndStatusNot(
            Long userId, Long semesterId, String status);

    default Optional<TuitionFee> findFirstUnpaidByUserIdAndSemesterId(Long userId, Long semesterId) {
        return findFirstByUserIdAndSemesterIdAndStatusNot(userId, semesterId, "đã đóng đủ");
    }

    List<TuitionFee> findByUserIdAndStatus(Long userId, String status);

    @Query("SELECT COALESCE(SUM(t.remainingAmount), 0) FROM TuitionFee t " +
            "WHERE t.userId = :userId AND t.status != 'đã đóng đủ'")
    BigDecimal sumRemainingByUserId(@Param("userId") Long userId);
}
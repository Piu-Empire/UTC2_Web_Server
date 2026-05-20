package com.utc2.appreborn.backend.modules.finance.repository;

import com.utc2.appreborn.backend.modules.finance.entity.TuitionFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TuitionFeeRepository extends JpaRepository<TuitionFee, Long> {

    // FIX 1: TuitionFee không có field "semester" — đúng là "semesterId" (Long)
    // Sắp xếp theo semesterId giảm dần (kỳ mới nhất lên đầu)
    List<TuitionFee> findByUserIdOrderBySemesterIdDesc(Long userId);

    // FIX 2: "semester" → "semesterId", type String → Long
    Optional<TuitionFee> findByUserIdAndSemesterId(Long userId, Long semesterId);

    // FIX 3: PaymentStatus enum → String (entity dùng String, không phải enum)
    List<TuitionFee> findByUserIdAndStatus(Long userId, String status);

    @Query("SELECT COALESCE(SUM(t.remainingAmount), 0) FROM TuitionFee t " +
           "WHERE t.user.id = :userId AND t.status != 'đã đóng đủ'")
    BigDecimal sumRemainingByUserId(@Param("userId") Long userId);
}
package com.utc2.appreborn.backend.modules.finance.repository;

import com.utc2.appreborn.backend.modules.finance.entity.TuitionFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TuitionFeeRepository extends JpaRepository<TuitionFee, Long> {

    // Sắp xếp theo semesterId giảm dần (kỳ mới nhất lên đầu)
    List<TuitionFee> findByUserIdOrderBySemesterIdDesc(Long userId);

    /** Chỉ lấy học phí môn học (feeType = SUBJECT) */
    List<TuitionFee> findByUserIdAndFeeTypeOrderBySemesterIdDesc(Long userId, String feeType);

    /** Chỉ lấy kỳ đã đóng đủ của 1 loại phí */
    List<TuitionFee> findByUserIdAndFeeTypeAndStatus(Long userId, String feeType, String status);

    // ── DORMITORY fee ─────────────────────────────────────────────────────────

    /** Lấy fee KTX theo dorm_reg_id — dùng để check đã tạo fee chưa */
    Optional<TuitionFee> findByDormRegId(Long dormRegId);

    /** Xóa fee KTX khi hủy đăng ký KTX (chưa đóng tiền) */
    void deleteByDormRegIdAndStatus(Long dormRegId, String status);

    /**
     * FIX: đổi trả về từ Optional → List để xử lý trường hợp sinh viên
     * đăng ký thêm môn SAU KHI đã đóng tiền → backend tạo fee record mới
     * cho phần tăng thêm → cùng (userId, semesterId) có nhiều fee records.
     *
     * Caller cũ dùng .orElseThrow() nên cần update sang dùng
     * findFirstByUserIdAndSemesterId() cho các flow payTuition/getTuitionBySemester,
     * và findAllByUserIdAndSemesterId() cho flow tính tổng nợ.
     */
    List<TuitionFee> findAllByUserIdAndSemesterIdAndFeeType(Long userId, Long semesterId, String feeType);

    /**
     * Giữ lại để tương thích với ImportServiceImpl.
     * Trả về Optional — lấy fee đầu tiên tìm thấy theo (userId, semesterId).
     */
    Optional<TuitionFee> findByUserIdAndSemesterId(Long userId, Long semesterId);

    /**
     * Lấy fee SUBJECT chưa đóng đủ của (user, semester) — dùng trong enroll/cancel
     * để update lại tổng tiền khi tín chỉ thay đổi.
     * Chỉ lấy record "chưa đóng" hoặc "đóng một phần" (không đụng vào record đã đóng đủ).
     */
    Optional<TuitionFee> findFirstByUserIdAndSemesterIdAndFeeTypeAndStatusNot(
            Long userId, Long semesterId, String feeType, String status);

    /**
     * Lấy 1 fee record theo (userId, semesterId) — giữ lại cho các flow
     * payTuition / getTuitionBySemester không cần phân biệt nhiều records.
     * Ưu tiên record "chưa đóng" trước (status ASC: "chưa đóng" < "đã đóng đủ").
     */
    @Query("SELECT t FROM TuitionFee t " +
            "WHERE t.userId = :userId AND t.semesterId = :semesterId AND t.feeType = 'SUBJECT' " +
            "ORDER BY CASE t.status " +
            "  WHEN 'chưa đóng' THEN 0 WHEN 'đóng một phần' THEN 1 ELSE 2 END ASC")
    Optional<TuitionFee> findFirstUnpaidByUserIdAndSemesterId(
            @Param("userId") Long userId,
            @Param("semesterId") Long semesterId);

    List<TuitionFee> findByUserIdAndStatus(Long userId, String status);

    /**
     * FIX: tính tổng nợ trên TẤT CẢ fee records chưa đóng đủ của user —
     * bao gồm cả fee phụ được tạo khi đăng ký thêm môn sau khi đã đóng tiền.
     * Query cũ không đổi, nhưng giờ đã đúng vì có nhiều records trên cùng kỳ.
     */
    @Query("SELECT COALESCE(SUM(t.remainingAmount), 0) FROM TuitionFee t " +
            "WHERE t.userId = :userId AND t.status != 'đã đóng đủ'")
    BigDecimal sumRemainingByUserId(@Param("userId") Long userId);
}
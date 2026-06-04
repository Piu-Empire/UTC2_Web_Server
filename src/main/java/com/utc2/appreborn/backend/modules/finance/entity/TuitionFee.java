package com.utc2.appreborn.backend.modules.finance.entity;

import com.utc2.appreborn.backend.modules.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee")        // ← khớp DB
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TuitionFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fee_id")            // ← khớp DB
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // FIX: field riêng để repository findByUserId/sumRemainingByUserId hoạt động
    // insertable=false, updatable=false vì user_id do @JoinColumn quản lý
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "semester_id")       // ← FK → SEMESTER (nullable với DORMITORY)
    private Long semesterId;

    /**
     * FK → dormitory_registration — chỉ set khi feeType = DORMITORY.
     */
    @Column(name = "dorm_reg_id")
    private Long dormRegId;

    /**
     * Loại phí: "SUBJECT" (học phí môn học) / "DORMITORY" / "OTHER"
     * Mặc định SUBJECT để tương thích ngược với data cũ.
     * Dùng để phân biệt khi query — tránh lẫn các loại phí trong cùng bảng fee.
     */
    @Column(name = "fee_type", length = 50)
    private String feeType;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 15, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "remaining_amount", precision = 15, scale = 2)
    private BigDecimal remainingAmount;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "status")            // "chưa đóng" / "đóng một phần" / "đã đóng đủ"
    private String status;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "paid_at")           // ← DB dùng paid_at, không phải paidDate
    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) status = "chưa đóng";
        if (feeType == null) feeType = "SUBJECT"; // mặc định học phí môn học
        if (paidAmount == null) paidAmount = BigDecimal.ZERO;
        if (totalAmount != null) {
            remainingAmount = totalAmount.subtract(paidAmount);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (totalAmount != null && paidAmount != null) {
            remainingAmount = totalAmount.subtract(paidAmount);
        }
    }
}
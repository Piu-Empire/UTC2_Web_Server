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

    @Column(name = "semester_id")       // ← FK → SEMESTER
    private Long semesterId;

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
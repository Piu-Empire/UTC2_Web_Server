-- ============================================================
-- V4__fix_ui_breaking_columns.sql
-- Migration chỉ sửa các cột ảnh hưởng trực tiếp đến UI
-- Áp dụng trên DB đang chạy, không cần drop/recreate
-- ============================================================

-- ── 1. USER ──────────────────────────────────────────────────
-- LoginActivity → AuthServiceImpl load UserDetails theo username (email)
-- Spring Security UserDetails cần field `role` và `enabled` tồn tại trong bảng.
-- Thiếu 2 cột này → Hibernate map lỗi → đăng nhập không được.
ALTER TABLE `user`
    ADD COLUMN `role`    VARCHAR(50) NOT NULL DEFAULT 'STUDENT'
        COMMENT 'Vai trò: STUDENT / ADMIN / STAFF',
    ADD COLUMN `enabled` BOOLEAN     NOT NULL DEFAULT true
        COMMENT '1: tài khoản đang hoạt động, 0: bị khoá';

-- ── 2. STUDENT_PROFILE ───────────────────────────────────────
-- student_card_url đã được thêm ở V4__add_student_card_url.sql, bỏ qua.

-- ── 3. fee ───────────────────────────────────────────────────
-- SubjectTuitionActivity / DormitoryTuitionActivity / InvoiceActivity
-- gọi TuitionController → TuitionFeeRepository.
-- Khi sinh viên chưa đóng tiền, backend insert FEE với paid_at = NULL.
-- Nếu cột là NOT NULL → INSERT lỗi 500 → màn hình học phí trắng.
ALTER TABLE `fee`
    MODIFY COLUMN `paid_at` TIMESTAMP NULL DEFAULT NULL
        COMMENT 'Thời điểm thanh toán (NULL nếu chưa đóng)';

-- Sửa data sai trong seed: row chưa đóng đang có paid_at = timestamp giả
UPDATE `fee`
    SET `paid_at` = NULL, `payment_method` = NULL
    WHERE `status` = 'chưa đóng' AND `paid_amount` = 0;

-- ── 4. service_request ───────────────────────────────────────
-- CardReissueActivity, LoanSupportActivity, TranscriptRegistrationActivity,
-- StudentConfirmationActivity → PublicServicesController POST các endpoint
-- → ServiceRequest entity @PrePersist set submittedAt, resolvedAt = NULL.
-- Nếu cột resolved_at là NOT NULL → INSERT lỗi → submit dịch vụ thất bại.
ALTER TABLE `service_request`
    MODIFY COLUMN `resolved_at` TIMESTAMP NULL DEFAULT NULL
        COMMENT 'Thời điểm xử lý xong (NULL nếu chưa xong)';

-- Sửa data sai trong seed: row đang chờ xử lý có resolved_at = timestamp giả
UPDATE `service_request`
    SET `resolved_at` = NULL
    WHERE `status` = 'chờ xử lý' OR `status` = 'PENDING';
-- ============================================================
-- V12__simplify_payment_status.sql
-- Đơn giản hóa trạng thái học phí: chỉ còn "chưa đóng" / "đã đóng đủ"
-- Bỏ "đóng một phần" vì app chỉ cho đóng online 1 lần đủ
-- ============================================================

-- ── 1. Sửa data seed sai: "đóng một phần" → "chưa đóng" ─────
-- Sinh viên chưa đóng đủ thì coi như chưa đóng,
-- paid_amount và remaining_amount reset lại cho nhất quán
UPDATE `fee`
    SET `status`           = 'chưa đóng',
        `paid_amount`      = 0,
        `remaining_amount` = `total_amount`,
        `paid_at`          = NULL,
        `payment_method`   = NULL
    WHERE `status` = 'đóng một phần';

-- ── 2. Thêm CHECK CONSTRAINT để DB không cho insert trạng thái lạ ──
-- (MySQL 8.0.16+ mới enforce CHECK, bỏ qua nếu dùng MySQL < 8.0.16)
ALTER TABLE `fee`
    ADD CONSTRAINT `chk_fee_status`
    CHECK (`status` IN ('chưa đóng', 'đã đóng đủ'));

-- ── 3. Sửa dormitory_registration.paid_status tương tự ───────
-- paid_status của KTX cũng chỉ cần "chưa đóng" / "đã đóng"
ALTER TABLE `dormitory_registration`
    ADD CONSTRAINT `chk_dorm_paid_status`
    CHECK (`paid_status` IN ('chưa đóng', 'đã đóng'));
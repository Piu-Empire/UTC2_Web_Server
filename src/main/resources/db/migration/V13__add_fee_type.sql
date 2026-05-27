-- ============================================================
-- V7__add_fee_type.sql
-- Thêm cột fee_type để phân biệt học phí môn học / loại phí khác
-- Tương thích ngược: data cũ mặc định là SUBJECT
-- ============================================================

ALTER TABLE `fee`
    ADD COLUMN `fee_type` VARCHAR(50) NOT NULL DEFAULT 'SUBJECT'
        COMMENT 'Loại phí: SUBJECT (học phí môn học) / DORMITORY / OTHER';

-- Update data cũ cho chắc chắn (dù DEFAULT đã xử lý)
UPDATE `fee` SET `fee_type` = 'SUBJECT' WHERE `fee_type` IS NULL OR `fee_type` = '';

-- CHECK CONSTRAINT đảm bảo chỉ cho giá trị hợp lệ
ALTER TABLE `fee`
    ADD CONSTRAINT `chk_fee_type`
    CHECK (`fee_type` IN ('SUBJECT', 'DORMITORY', 'OTHER'));
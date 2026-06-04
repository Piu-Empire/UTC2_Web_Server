-- ============================================================
-- V14__fee_link_enrollment_and_dormitory.sql
-- Cho phép bảng fee lưu học phí KTX (không cần semester_id)
-- và liên kết với dorm_reg_id để xác định đăng ký KTX tương ứng.
-- ============================================================

-- 1. Bỏ NOT NULL constraint trên semester_id để DORMITORY fee không cần semester
ALTER TABLE `fee`
    MODIFY COLUMN `semester_id` BIGINT NULL
    COMMENT 'FK -> semester (NULL với fee_type=DORMITORY)';

-- 2. Thêm cột dorm_reg_id để liên kết fee KTX với dormitory_registration
ALTER TABLE `fee`
    ADD COLUMN `dorm_reg_id` BIGINT NULL
        COMMENT 'FK -> dormitory_registration (chỉ dùng khi fee_type=DORMITORY)';

-- 3. FK constraint cho dorm_reg_id
ALTER TABLE `fee`
    ADD CONSTRAINT `fk_fee_dorm_reg`
        FOREIGN KEY (`dorm_reg_id`) REFERENCES `dormitory_registration`(`dorm_reg_id`)
            ON UPDATE NO ACTION ON DELETE SET NULL;
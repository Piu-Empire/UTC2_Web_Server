-- ============================================================
-- Thêm workflow duyệt cho: leaderboard, scholarship, warning
-- ============================================================

-- 1. Bảng duyệt bảng xếp hạng theo kỳ
CREATE TABLE IF NOT EXISTS `leaderboard_approval` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT,
    `semester_id` BIGINT NOT NULL COMMENT 'Kỳ học được duyệt',
    `approved_by` BIGINT NOT NULL COMMENT 'user_id của staff lv5/admin duyệt',
    `approved_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_lb_semester` (`semester_id`),
    CONSTRAINT `fk_lba_semester` FOREIGN KEY (`semester_id`) REFERENCES `semester`(`semester_id`),
    CONSTRAINT `fk_lba_user`     FOREIGN KEY (`approved_by`) REFERENCES `user`(`user_id`)
);

-- 2. Thêm cột status vào student_scholarship để hỗ trợ pending
-- pending   = đã import, chưa duyệt (ẩn app)
-- approved  = đã duyệt, chưa nhận (hiển thị app)
-- received  = đã nhận (hiển thị app)
ALTER TABLE `student_scholarship`
    ADD COLUMN pending_status VARCHAR(20) DEFAULT 'pending',
    ADD COLUMN approved_by BIGINT NULL,
    ADD COLUMN approved_at TIMESTAMP NULL;

-- 3. Thêm cột vào academic_warning để hỗ trợ pending + expire
-- pending          = đã import, chưa duyệt (ẩn app)
-- ACTIVE           = đang hiệu lực (hiển thị app)
-- EXPIRED          = hết hiệu lực (tự chuyển sau 6 tháng)
ALTER TABLE `academic_warning`
    ADD COLUMN approved_by BIGINT NULL,
    ADD COLUMN approved_at TIMESTAMP NULL;
-- Đổi các giá trị status cũ về chuẩn mới
UPDATE `academic_warning` SET `status` = 'ACTIVE'   WHERE `status` = 'đang hiệu lực' OR `status` = 'active';
UPDATE `academic_warning` SET `status` = 'EXPIRED'  WHERE `status` = 'RESOLVED' OR `status` = 'resolved' OR `status` = 'đã giải quyết';

-- Mặc định warning cũ đã có approved_at = issued_at (coi như đã duyệt)
UPDATE `academic_warning` SET `approved_at` = `issued_at` WHERE `approved_at` IS NULL AND `status` IN ('ACTIVE','EXPIRED');
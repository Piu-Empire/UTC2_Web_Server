-- ============================================================
-- V20250815_01: Assessment – staff_level + external redesign
-- ============================================================

-- 1. Thêm staff_level vào bảng user (NULL nếu không phải STAFF)
ALTER TABLE `user`
    ADD COLUMN `staff_level` INT NULL COMMENT '1=tập thể lớp, 2=giảng viên, 3=bộ môn, 4=khoa, 5=phòng giáo vụ/trường. NULL nếu không phải STAFF';

-- 2. Thêm bo_mon_score vào external_assessment (tách ra từ khoa_score cũ)
ALTER TABLE `external_assessment`
    ADD COLUMN `bo_mon_score` DECIMAL(5,2) NOT NULL DEFAULT 0
        COMMENT 'Điểm bộ môn (staff lv3)' AFTER `tap_the_score`;

-- 3. Bảng trạng thái duyệt – mỗi sinh viên/học kỳ có 1 row
CREATE TABLE IF NOT EXISTS `external_assessment_status` (
    `user_id`           BIGINT       NOT NULL COMMENT 'FK -> user',
    `period_id`         VARCHAR(50)  NOT NULL COMMENT 'FK -> assessment_period',
    `advisor_approved`  BOOLEAN      NOT NULL DEFAULT FALSE COMMENT 'CVHT đã duyệt',
    `khoa_approved`     BOOLEAN      NOT NULL DEFAULT FALSE COMMENT 'Khoa đã duyệt',
    `truong_approved`   BOOLEAN      NOT NULL DEFAULT FALSE COMMENT 'Trường đã duyệt',
    `advisor_approved_at` TIMESTAMP  NULL,
    `khoa_approved_at`    TIMESTAMP  NULL,
    `truong_approved_at`  TIMESTAMP  NULL,
    PRIMARY KEY (`user_id`, `period_id`)
) COMMENT 'Trạng thái duyệt external assessment theo từng sinh viên/học kỳ';
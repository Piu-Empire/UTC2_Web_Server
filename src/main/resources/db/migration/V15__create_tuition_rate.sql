-- ============================================================
-- V15__create_tuition_rate.sql
-- Bảng giá tín chỉ — admin cấu hình, có thể đổi theo năm học
-- ============================================================

CREATE TABLE IF NOT EXISTS `tuition_rate` (
                                              `rate_id`          BIGINT NOT NULL AUTO_INCREMENT,
                                              `price_per_credit` DECIMAL(15,2) NOT NULL COMMENT 'Giá 1 tín chỉ (VNĐ)',
    `academic_year`    VARCHAR(50)   NULL     COMMENT 'Năm học áp dụng VD: 2024-2025 (NULL = mặc định)',
    `effective_from`   DATE          NOT NULL COMMENT 'Áp dụng từ ngày',
    `note`             VARCHAR(255)  NULL,
    PRIMARY KEY(`rate_id`)
    );

-- Giá mặc định hiện tại
INSERT INTO `tuition_rate` (`price_per_credit`, `academic_year`, `effective_from`, `note`)
VALUES (600000, NULL, '2024-09-01', 'Giá tín chỉ mặc định');
-- ============================================================
-- V4: Assessment module
-- Chức năng Đánh giá rèn luyện sinh viên
-- ============================================================

-- Học kỳ đánh giá
CREATE TABLE IF NOT EXISTS `assessment_period` (
    `period_id`      VARCHAR(50)  NOT NULL COMMENT 'Mã học kỳ, VD: HK1_2024_2025',
    `label`          VARCHAR(100) NOT NULL COMMENT 'Tên hiển thị, VD: Học kỳ 1 – 2024-2025',
    `is_active`      BOOLEAN      NOT NULL DEFAULT TRUE COMMENT 'Đang mở đánh giá hay không',
    `created_at`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`period_id`)
);

-- Sinh viên tự đánh giá rèn luyện (tab SV đánh giá)
-- Mỗi tiêu chí lưu 1 row: (student_id, period_id, criteria_id) là unique
CREATE TABLE IF NOT EXISTS `student_assessment` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`        BIGINT       NOT NULL COMMENT 'Khóa ngoại -> user (sinh viên)',
    `period_id`      VARCHAR(50)  NOT NULL COMMENT 'Khóa ngoại -> assessment_period',
    `criteria_id`    INT          NOT NULL COMMENT 'ID tiêu chí (khớp với app)',
    `score`          DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT 'Điểm sinh viên tự chọn',
    `evidence_uris`  TEXT         COMMENT 'Danh sách URI minh chứng, phân cách bởi |',
    `submitted_at`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_student_assessment` (`user_id`, `period_id`, `criteria_id`)
) COMMENT 'Sinh viên tự đánh giá — luồng App -> Server -> Admin (chỉ đọc chiều ngược)';

-- Sinh viên đánh giá cố vấn học tập
-- Mỗi tiêu chí CVHT lưu 1 row: (student_id, period_id, criteria_id) là unique
CREATE TABLE IF NOT EXISTS `advisor_assessment` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`        BIGINT       NOT NULL COMMENT 'Khóa ngoại -> user (sinh viên đánh giá)',
    `period_id`      VARCHAR(50)  NOT NULL COMMENT 'Khóa ngoại -> assessment_period',
    `criteria_id`    INT          NOT NULL COMMENT 'ID tiêu chí CVHT (id 100-111)',
    `score`          DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT 'Điểm sinh viên chọn (1-5)',
    `student_opinion` TEXT        COMMENT 'Ý kiến riêng của sinh viên (footer CVHT)',
    `submitted_at`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_advisor_assessment` (`user_id`, `period_id`, `criteria_id`)
) COMMENT 'Đánh giá CVHT — luồng App -> Server -> Admin (chỉ đọc chiều ngược)';

-- Điểm từ Tập thể lớp / Khoa/BM / Trường
-- Admin import CSV/Word rồi POST vào server, app chỉ đọc
CREATE TABLE IF NOT EXISTS `external_assessment` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`        BIGINT       NOT NULL COMMENT 'Khóa ngoại -> user (sinh viên được đánh giá)',
    `period_id`      VARCHAR(50)  NOT NULL COMMENT 'Khóa ngoại -> assessment_period',
    `criteria_id`    INT          NOT NULL COMMENT 'ID tiêu chí',
    `tap_the_score`  DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT 'Điểm tập thể lớp',
    `khoa_score`     DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT 'Điểm khoa/BM',
    `truong_score`   DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT 'Điểm nhà trường',
    `imported_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_external_assessment` (`user_id`, `period_id`, `criteria_id`)
) COMMENT 'Điểm external — luồng Admin -> Server -> App (chỉ đọc chiều ngược)';

-- Dữ liệu mẫu học kỳ
INSERT IGNORE INTO `assessment_period` (`period_id`, `label`, `is_active`) VALUES
    ('HK1_2024_2025', 'Học kỳ 1 – 2024-2025', FALSE),
    ('HK2_2024_2025', 'Học kỳ 2 – 2024-2025', FALSE),
    ('HK1_2025_2026', 'Học kỳ 1 – 2025-2026', TRUE),
    ('HK2_2025_2026', 'Học kỳ 2 – 2025-2026', FALSE);

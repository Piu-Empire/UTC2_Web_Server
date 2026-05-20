CREATE TABLE IF NOT EXISTS `scholarship` (
    `scholarship_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `name`           VARCHAR(255) NOT NULL COMMENT 'Tên học bổng',
    `organization`   VARCHAR(255) COMMENT 'Tổ chức cấp học bổng',
    `amount`         DECIMAL(15,2) COMMENT 'Giá trị học bổng (VNĐ)',
    `unit`           VARCHAR(20)  COMMENT 'Đơn vị: HK (học kỳ) hoặc năm',
    `min_gpa`        DECIMAL(3,2) COMMENT 'GPA tối thiểu để được xét',
    `description`    TEXT         COMMENT 'Mô tả chi tiết',
    PRIMARY KEY(`scholarship_id`)
);
CREATE TABLE IF NOT EXISTS `student_scholarship` (
    `id`             BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id`        BIGINT NOT NULL COMMENT 'Khóa ngoại -> user',
    `scholarship_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> scholarship',
    `status`         VARCHAR(50) COMMENT 'received | not_received',
    `semester_id`    BIGINT COMMENT 'Kỳ học nhận học bổng (có thể NULL nếu theo năm)',
    `received_at`    DATE COMMENT 'Ngày nhận học bổng',
    PRIMARY KEY(`id`)
);
ALTER TABLE `student_scholarship`
ADD FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE `student_scholarship`
ADD FOREIGN KEY(`scholarship_id`) REFERENCES `scholarship`(`scholarship_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;
-- ── Seed data học bổng ─────────────────────────────────────────────────────
INSERT INTO `scholarship`
(`name`, `organization`, `amount`, `unit`, `min_gpa`, `description`)
VALUES
('Học bổng Khuyến khích học tập',     'Trường ĐH Giao thông vận tải TP.HCM', 3000000, 'HK',  3.20, 'Dành cho sinh viên đạt GPA từ 3.2 trở lên mỗi học kỳ.'),
('Học bổng JICA',                     'Cơ quan Hợp tác Quốc tế Nhật Bản',   20000000,'năm', 3.70, 'Học bổng hợp tác với JICA, dành cho sinh viên xuất sắc.'),
('Học bổng Vallet',                   'Quỹ Vallet Việt Nam',                 15000000,'năm', 3.60, 'Tài trợ bởi Quỹ Vallet, ưu tiên ngành kỹ thuật và khoa học.'),
('Học bổng Vingroup',                 'Tập đoàn Vingroup',                    8000000,'HK',  3.50, 'Học bổng của Quỹ Thiện tâm Vingroup cho sinh viên nghèo vượt khó.'),
('Học bổng Chính phủ',               'Bộ Giáo dục và Đào tạo',              5000000,'HK',  3.40, 'Học bổng diện chính sách của Bộ GD-ĐT.'),
('Học bổng KKHT Loại A',             'Trường ĐH Giao thông vận tải TP.HCM', 4500000,'HK',  3.60, 'Xếp loại A: GPA >= 3.6 và không có môn dưới C.');
-- Gán học bổng cho sinh viên mẫu (user_id = 2)
INSERT INTO `student_scholarship`
(`user_id`, `scholarship_id`, `status`, `semester_id`, `received_at`)
VALUES
(2, 2, 'received',     1, '2025-12-15'),
(2, 5, 'received',     1, '2025-11-01'),
(2, 1, 'not_received', NULL, NULL),
(2, 3, 'not_received', NULL, NULL),
(2, 4, 'not_received', NULL, NULL),
(2, 6, 'not_received', NULL, NULL);
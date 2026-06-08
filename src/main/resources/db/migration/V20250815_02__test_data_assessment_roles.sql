-- ============================================================
-- V20250815_02: Test data – mỗi role 1 account + assessment data
-- Password tất cả accounts: 123456789
-- Hash: $2a$12$1r6iM2aHetFoGWmajB1EBegjLTRrjUjyB.SBD85TR/T51hYdDw9CC
-- ============================================================

-- ─── 1. Fix các user cũ chưa có role/enabled ─────────────────────────────────

-- user_id 1: admin gốc (chưa có role sau V2)
UPDATE `user`
SET `role` = 'ADMIN', `enabled` = TRUE,
    `password_hash` = '$2a$12$1r6iM2aHetFoGWmajB1EBegjLTRrjUjyB.SBD85TR/T51hYdDw9CC'
WHERE `email` = 'admin@utc2.edu.vn';

-- user_id 3,4,5: sinh viên
UPDATE `user`
SET `role` = 'STUDENT', `enabled` = TRUE,
    `password_hash` = '$2a$12$1r6iM2aHetFoGWmajB1EBegjLTRrjUjyB.SBD85TR/T51hYdDw9CC'
WHERE `email` IN (
    '2211020002@st.utc2.edu.vn',
    '2211030001@st.utc2.edu.vn',
    '6551071085@st.utc2.edu.vn'
);

-- ─── 2. Thêm users cho từng role/level ───────────────────────────────────────

-- user_id 6: ADVISOR (cố vấn học tập)
INSERT INTO `user` (`email`, `password_hash`, `auth_provider`, `role`, `staff_level`, `enabled`)
VALUES ('cvht@utc2.edu.vn',
        '$2a$12$1r6iM2aHetFoGWmajB1EBegjLTRrjUjyB.SBD85TR/T51hYdDw9CC',
        'local', 'ADVISOR', NULL, TRUE);

-- user_id 7: STAFF level 1 (tập thể lớp)
INSERT INTO `user` (`email`, `password_hash`, `auth_provider`, `role`, `staff_level`, `enabled`)
VALUES ('tapthe.lop@utc2.edu.vn',
        '$2a$12$1r6iM2aHetFoGWmajB1EBegjLTRrjUjyB.SBD85TR/T51hYdDw9CC',
        'local', 'STAFF', 1, TRUE);

-- user_id 8: STAFF level 2 (giảng viên)
INSERT INTO `user` (`email`, `password_hash`, `auth_provider`, `role`, `staff_level`, `enabled`)
VALUES ('giangvien@utc2.edu.vn',
        '$2a$12$1r6iM2aHetFoGWmajB1EBegjLTRrjUjyB.SBD85TR/T51hYdDw9CC',
        'local', 'STAFF', 2, TRUE);

-- user_id 9: STAFF level 3 (bộ môn)
INSERT INTO `user` (`email`, `password_hash`, `auth_provider`, `role`, `staff_level`, `enabled`)
VALUES ('bomon@utc2.edu.vn',
        '$2a$12$1r6iM2aHetFoGWmajB1EBegjLTRrjUjyB.SBD85TR/T51hYdDw9CC',
        'local', 'STAFF', 3, TRUE);

-- user_id 10: STAFF level 4 (khoa)
INSERT INTO `user` (`email`, `password_hash`, `auth_provider`, `role`, `staff_level`, `enabled`)
VALUES ('khoa@utc2.edu.vn',
        '$2a$12$1r6iM2aHetFoGWmajB1EBegjLTRrjUjyB.SBD85TR/T51hYdDw9CC',
        'local', 'STAFF', 4, TRUE);

-- user_id 11: STAFF level 5 (phòng giáo vụ/trường)
INSERT INTO `user` (`email`, `password_hash`, `auth_provider`, `role`, `staff_level`, `enabled`)
VALUES ('pgv@utc2.edu.vn',
        '$2a$12$1r6iM2aHetFoGWmajB1EBegjLTRrjUjyB.SBD85TR/T51hYdDw9CC',
        'local', 'STAFF', 5, TRUE);

-- ─── 3. user_profile cho các user mới ────────────────────────────────────────

INSERT INTO `user_profile` (`user_id`, `full_name`, `phone_number`, `date_of_birth`, `gender`)
SELECT `user_id`, 'Nguyễn Thị CVHT',       '0901000006', '1985-03-10', 'Nữ'  FROM `user` WHERE `email` = 'cvht@utc2.edu.vn';

INSERT INTO `user_profile` (`user_id`, `full_name`, `phone_number`, `date_of_birth`, `gender`)
SELECT `user_id`, 'Lớp trưởng 65TH1',      '0901000007', '2004-06-15', 'Nam' FROM `user` WHERE `email` = 'tapthe.lop@utc2.edu.vn';

INSERT INTO `user_profile` (`user_id`, `full_name`, `phone_number`, `date_of_birth`, `gender`)
SELECT `user_id`, 'Giảng viên CNTT',        '0901000008', '1982-05-18', 'Nam' FROM `user` WHERE `email` = 'giangvien@utc2.edu.vn';

INSERT INTO `user_profile` (`user_id`, `full_name`, `phone_number`, `date_of_birth`, `gender`)
SELECT `user_id`, 'GV Bộ môn CNTT',         '0901000009', '1980-09-20', 'Nam' FROM `user` WHERE `email` = 'bomon@utc2.edu.vn';

INSERT INTO `user_profile` (`user_id`, `full_name`, `phone_number`, `date_of_birth`, `gender`)
SELECT `user_id`, 'Trưởng khoa CNTT',       '0901000010', '1975-12-05', 'Nam' FROM `user` WHERE `email` = 'khoa@utc2.edu.vn';

INSERT INTO `user_profile` (`user_id`, `full_name`, `phone_number`, `date_of_birth`, `gender`)
SELECT `user_id`, 'Phòng Giáo vụ / Trường', '0901000011', '1978-07-22', 'Nữ'  FROM `user` WHERE `email` = 'pgv@utc2.edu.vn';

-- ─── 4. Sinh viên tự đánh giá (student_assessment) ───────────────────────────
-- Học kỳ HK1_2025_2026 | criteria_id 1-5 khớp với app
-- SV Trần Thị B (user_id 3) — đã nộp đủ
INSERT IGNORE INTO `student_assessment`
    (`user_id`, `period_id`, `criteria_id`, `score`, `evidence_uris`)
VALUES
(3, 'HK1_2025_2026', 1, 18.00, NULL),
(3, 'HK1_2025_2026', 2, 17.00, NULL),
(3, 'HK1_2025_2026', 3, 15.00, NULL),
(3, 'HK1_2025_2026', 4, 10.00, NULL),
(3, 'HK1_2025_2026', 5,  8.00, NULL);

-- SV Cao Trung Tinh (user_id 5) — đã nộp đủ + có minh chứng
INSERT IGNORE INTO `student_assessment`
    (`user_id`, `period_id`, `criteria_id`, `score`, `evidence_uris`)
VALUES
(5, 'HK1_2025_2026', 1, 20.00, 'content://media/image/001.jpg'),
(5, 'HK1_2025_2026', 2, 18.00, 'content://media/image/002.jpg|content://media/image/003.jpg'),
(5, 'HK1_2025_2026', 3, 15.00, NULL),
(5, 'HK1_2025_2026', 4, 12.00, NULL),
(5, 'HK1_2025_2026', 5, 10.00, NULL);

-- ─── 5. Đánh giá CVHT (advisor_assessment) ───────────────────────────────────
-- criteria_id 100-104 khớp với tiêu chí CVHT trong app
INSERT IGNORE INTO `advisor_assessment`
    (`user_id`, `period_id`, `criteria_id`, `score`, `student_opinion`)
VALUES
-- SV Trần Thị B (user_id 3): đánh giá CVHT khá tốt
(3, 'HK1_2025_2026', 100, 4.00, NULL),
(3, 'HK1_2025_2026', 101, 4.00, NULL),
(3, 'HK1_2025_2026', 102, 5.00, NULL),
(3, 'HK1_2025_2026', 103, 4.00, NULL),
(3, 'HK1_2025_2026', 104, 3.00, NULL),
(3, 'HK1_2025_2026', 105, 4.00, NULL),
(3, 'HK1_2025_2026', 106, 4.00, NULL),
(3, 'HK1_2025_2026', 107, 5.00, NULL),
(3, 'HK1_2025_2026', 108, 4.00, NULL),
(3, 'HK1_2025_2026', 109, 3.00, NULL),
(3, 'HK1_2025_2026', 110, 4.00, NULL),
(3, 'HK1_2025_2026', 111, 5.00, 'CVHT nhiệt tình, hỗ trợ tốt'),
-- SV Cao Trung Tinh (user_id 5): đánh giá CVHT xuất sắc
(5, 'HK1_2025_2026', 100, 5.00, NULL),
(5, 'HK1_2025_2026', 101, 5.00, NULL),
(5, 'HK1_2025_2026', 102, 4.00, NULL),
(5, 'HK1_2025_2026', 103, 5.00, NULL),
(5, 'HK1_2025_2026', 104, 5.00, NULL),
(5, 'HK1_2025_2026', 105, 5.00, NULL),
(5, 'HK1_2025_2026', 106, 4.00, NULL),
(5, 'HK1_2025_2026', 107, 5.00, NULL),
(5, 'HK1_2025_2026', 108, 5.00, NULL),
(5, 'HK1_2025_2026', 109, 4.00, NULL),
(5, 'HK1_2025_2026', 110, 5.00, NULL),
(5, 'HK1_2025_2026', 111, 5.00, 'Rất hài lòng với cố vấn học tập');

-- ─── 6. External scores (external_assessment) ────────────────────────────────
-- SV Trần Thị B (user_id 3):
--   Staff lv1 đã nhập tap_the, chưa có gì khác
INSERT IGNORE INTO `external_assessment`
    (`user_id`, `period_id`, `criteria_id`,
     `tap_the_score`, `bo_mon_score`, `khoa_score`, `truong_score`)
VALUES
(3, 'HK1_2025_2026', 1, 17.00, 0.00, 0.00, 0.00),
(3, 'HK1_2025_2026', 2, 16.00, 0.00, 0.00, 0.00),
(3, 'HK1_2025_2026', 3, 14.00, 0.00, 0.00, 0.00),
(3, 'HK1_2025_2026', 4,  9.00, 0.00, 0.00, 0.00),
(3, 'HK1_2025_2026', 5,  8.00, 0.00, 0.00, 0.00);

-- SV Cao Trung Tinh (user_id 5):
--   Advisor đã duyệt → staff lv3,lv4,lv5 đã nhập đủ
INSERT IGNORE INTO `external_assessment`
    (`user_id`, `period_id`, `criteria_id`,
     `tap_the_score`, `bo_mon_score`, `khoa_score`, `truong_score`)
VALUES
(5, 'HK1_2025_2026', 1, 19.00, 18.00, 19.00, 19.00),
(5, 'HK1_2025_2026', 2, 18.00, 17.00, 17.00, 18.00),
(5, 'HK1_2025_2026', 3, 14.00, 15.00, 15.00, 14.00),
(5, 'HK1_2025_2026', 4, 12.00, 11.00, 12.00, 12.00),
(5, 'HK1_2025_2026', 5,  9.00, 10.00,  9.00, 10.00);

-- ─── 7. Approval status (external_assessment_status) ─────────────────────────
-- SV Trần Thị B (3): Tap the đã nhập nhưng CVHT chưa duyệt
INSERT IGNORE INTO `external_assessment_status`
    (`user_id`, `period_id`, `advisor_approved`, `khoa_approved`, `truong_approved`)
VALUES (3, 'HK1_2025_2026', FALSE, FALSE, FALSE);

-- SV Cao Trung Tinh (5): CVHT + Khoa đã duyệt, Trường chưa
INSERT IGNORE INTO `external_assessment_status`
    (`user_id`, `period_id`,
     `advisor_approved`, `advisor_approved_at`,
     `khoa_approved`,    `khoa_approved_at`,
     `truong_approved`)
VALUES (5, 'HK1_2025_2026',
        TRUE,  '2025-11-10 14:30:00',
        TRUE,  '2025-11-15 09:00:00',
        FALSE);

-- ============================================================
-- V20250815_03: Hoàn chỉnh phân quyền theo sơ đồ
--
-- Sơ đồ roles:
--   STUDENT  : sinh viên
--   ADVISOR  : CVHT — quyền ngang staff lv2
--   STAFF    : staff_level phân cấp:
--                lv1 = tập thể lớp   → nhập tap_the_score
--                lv2 = giảng viên    → (chức năng khác, không nhập external)
--                lv3 = bộ môn        → nhập bo_mon_score
--                lv4 = khoa          → nhập khoa_score + duyệt
--                lv5 = phòng GV/trường → nhập truong_score + duyệt
--   ADMIN    : quản trị toàn hệ thống
-- ============================================================

-- 1. Cập nhật comment cột staff_level cho đúng với sơ đồ
ALTER TABLE `user`
    MODIFY COLUMN `staff_level` INT NULL
    COMMENT '1=tập thể lớp, 2=giảng viên, 3=bộ môn, 4=khoa, 5=phòng giáo vụ/trường. NULL nếu không phải STAFF';

-- 2. Thêm account test STAFF level 2 (giảng viên) — còn thiếu từ V20250815_02
INSERT IGNORE INTO `user` (`email`, `password_hash`, `auth_provider`, `role`, `staff_level`, `enabled`)
VALUES ('giangvien@utc2.edu.vn',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHHi',
        'local', 'STAFF', 2, TRUE);

INSERT IGNORE INTO `user_profile` (`user_id`, `full_name`, `phone_number`, `date_of_birth`, `gender`)
SELECT `user_id`, 'Giảng viên CNTT', '0901000011', '1982-05-18', 'Nam'
FROM `user` WHERE `email` = 'giangvien@utc2.edu.vn';

-- 3. Đổi tên email phòng GV/trường cho đúng nghĩa hơn (nếu chưa tồn tại email mới)
--    Nếu email cũ vẫn còn thì đổi tên hiển thị trong profile
UPDATE `user_profile`
SET `full_name` = 'Phòng Giáo vụ / Trường'
WHERE `user_id` = (SELECT `user_id` FROM `user` WHERE `email` = 'truong.ctsv@utc2.edu.vn');
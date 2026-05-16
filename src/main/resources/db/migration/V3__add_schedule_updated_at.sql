-- V3: Thêm cột updated_at vào bảng schedule để track thời điểm cập nhật lịch
-- App sẽ dùng giá trị MAX(updated_at) của user để so sánh với file local

ALTER TABLE `schedule`
ADD COLUMN `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Cập nhật dữ liệu mẫu có sẵn
UPDATE `schedule` SET `updated_at` = NOW();

-- ============================================================
-- V20250815_04: Xóa test assessment data không hợp lệ
-- Giữ nguyên: user accounts, assessment_period
-- Xóa: student_assessment, advisor_assessment, external_assessment,
--       external_assessment_status cho user_id 3 và 5
-- Lý do: test data dùng criteria 1-5 với điểm vượt quá max (5đ/tiêu chí)
--         gây hiển thị sai trên App
-- ============================================================

DELETE FROM `external_assessment_status`
WHERE `user_id` IN (3, 5);

DELETE FROM `external_assessment`
WHERE `user_id` IN (3, 5);

DELETE FROM `advisor_assessment`
WHERE `user_id` IN (3, 5);

DELETE FROM `student_assessment`
WHERE `user_id` IN (3, 5);
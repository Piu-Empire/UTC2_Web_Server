-- Fix warning_type values để khớp với App mapping:
--   App: "FAILED_EXAM" → "Điểm thi không đạt"
--   App: "LOW_GPA"     → "GPA thấp"
--   App: "ATTENDANCE"  → "Vắng mặt quá mức"
UPDATE `academic_warning` SET `warning_type` = 'LOW_GPA' WHERE `warning_type` = 'GPA thấp';

-- Thêm cảnh báo mẫu cho sinh viên user_id=2 (sinh viên test chính)
INSERT INTO `academic_warning`
(`user_id`, `semester_id`, `warning_type`, `description`, `resolved_at`, `status`)
VALUES
(2, 1, 'LOW_GPA',     'GPA học kỳ thấp hơn 2.5, cần cải thiện kết quả học tập.', NULL,                        'ACTIVE'),
(2, 1, 'ATTENDANCE',  'Vắng mặt quá 20% số buổi học môn Cơ sở dữ liệu.',         '2026-01-10 00:00:00', 'RESOLVED');

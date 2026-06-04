-- ── user ──────────────────────────────────────────────────────────────────────
INSERT INTO `user` (`email`, `password_hash`, `auth_provider`) VALUES
('admin@utc2.edu.vn',         '$2a$12$1r6iM2aHetFoGWmajB1EBegjLTRrjUjyB.SBD85TR/T51hYdDw9CC', 'local'),
('2211020001@st.utc2.edu.vn', '$2a$12$1r6iM2aHetFoGWmajB1EBegjLTRrjUjyB.SBD85TR/T51hYdDw9CC', 'local'),
('2211020002@st.utc2.edu.vn', '$2a$12$1r6iM2aHetFoGWmajB1EBegjLTRrjUjyB.SBD85TR/T51hYdDw9CC', 'local'),
('2211030001@st.utc2.edu.vn', '$2a$12$1r6iM2aHetFoGWmajB1EBegjLTRrjUjyB.SBD85TR/T51hYdDw9CC', 'local'),
('6551071085@st.utc2.edu.vn', '$2a$12$1r6iM2aHetFoGWmajB1EBegjLTRrjUjyB.SBD85TR/T51hYdDw9CC', 'local');
-- user_id: admin=1, sv_a=2, sv_b=3, sv_c=4, sv_tinh=5

-- ── advisor ────────────────────────────────────────────────────────────────────
INSERT INTO `advisor` (`full_name`, `email`, `phone`, `faculty`, `office_room`) VALUES
('Nguyễn Văn Hùng', 'hung.nguyen@utc2.edu.vn', '0909123456', 'Công nghệ thông tin', 'A101'),
('Trần Thị Mai',    'mai.tran@utc2.edu.vn',    '0911222333', 'Kinh tế vận tải',     'B203');

-- ── user_profile ───────────────────────────────────────────────────────────────
INSERT INTO `user_profile` (`user_id`, `full_name`, `phone_number`, `avatar_url`, `date_of_birth`, `gender`) VALUES
(1, 'Quản Trị Viên', '0909000000', 'https://cdn.utc2/avatar/admin.png',  '1990-01-01', 'Nam'),
(2, 'Nguyễn Văn A',  '0911111111', 'https://cdn.utc2/avatar/sv_a.png',   '2004-10-15', 'Nam'),
(3, 'Trần Thị B',    '0922222222', 'https://cdn.utc2/avatar/sv_b.png',   '2005-03-22', 'Nữ'),
(4, 'Lê Văn C',      '0933333333', 'https://cdn.utc2/avatar/sv_c.png',   '2004-12-01', 'Nam'),
(5, 'Cao Trung Tinh','0944444444', 'https://cdn.utc2/avatar/sv_tinh.png','2006-10-18', 'Nam');

-- ── student_profile ────────────────────────────────────────────────────────────
INSERT INTO `student_profile` (`user_id`, `student_code`, `faculty`, `advisor_id`, `major`, `academic_year`, `class_name`, `status`) VALUES
(2, '2211020001', 'Công nghệ thông tin', 1, 'Kỹ thuật phần mềm', 'K65', '65TH1', 'đang học'),
(3, '2211020002', 'Công nghệ thông tin', 1, 'Khoa học máy tính',  'K65', '65TH2', 'đang học'),
(4, '2211030001', 'Kinh tế vận tải',     2, 'Logistics',           'K65', '65LG1', 'đang học'),
(5, '6551071085', 'Công nghệ thông tin', 1, 'Kỹ thuật phần mềm', 'K65', '65TH3', 'đang học');

-- ── semester ───────────────────────────────────────────────────────────────────
-- Mỗi dòng là học kỳ theo từng sinh viên (gpa/credits lưu tại đây)
INSERT INTO `semester` (`user_id`, `semester_name`, `academic_year`, `semester_number`, `start_date`, `end_date`, `gpa`, `total_credits`, `passed_credits`) VALUES
(2, 'Học kỳ 1 năm 2025-2026', '2025-2026', 1, '2025-09-01', '2026-01-15', 3.45, 18, 18),  -- semester_id=1
(3, 'Học kỳ 1 năm 2025-2026', '2025-2026', 1, '2025-09-01', '2026-01-15', 3.10, 17, 15),  -- semester_id=2
(4, 'Học kỳ 1 năm 2025-2026', '2025-2026', 1, '2025-09-01', '2026-01-15', 2.85, 16, 14),  -- semester_id=3
(5, 'Học kỳ 1 năm 2025-2026', '2025-2026', 1, '2025-09-01', '2026-01-15', 3.75, 19, 19);  -- semester_id=4

-- ── course ─────────────────────────────────────────────────────────────────────
INSERT INTO `course` (`course_code`, `course_name`, `credits`, `theory_hours`, `practice_hours`, `department`, `description`) VALUES
('INT101', 'Nhập môn lập trình', 3, 30, 30, 'CNTT', 'Môn học cơ sở lập trình'),   -- course_id=1
('DBS201', 'Cơ sở dữ liệu',      3, 30, 30, 'CNTT', 'Thiết kế và quản trị CSDL'), -- course_id=2
('WEB301', 'Lập trình Web',       4, 45, 30, 'CNTT', 'Xây dựng ứng dụng web'),     -- course_id=3
('LOG101', 'Nhập môn Logistics',  3, 30, 15, 'KTVT', 'Kiến thức cơ bản logistics');-- course_id=4

-- ── class_section ──────────────────────────────────────────────────────────────
-- Lớp học phần: mỗi môn trong 1 học kỳ có thể mở nhiều lớp (01, 02...)
INSERT INTO `class_section` (`course_id`, `semester_id`, `section_code`, `lecturer_name`, `max_capacity`, `current_enrollment`, `room`, `building`, `section_type`, `status`) VALUES
(1, 1, 'INT101-01', 'TS. Phạm Minh',  40, 35, 'A101', 'Nhà A', 'lý thuyết', 'đang học'), -- section_id=1
(2, 1, 'DBS201-01', 'ThS. Lê Huy',    40, 30, 'B202', 'Nhà B', 'lý thuyết', 'đang học'), -- section_id=2
(1, 2, 'INT101-02', 'TS. Phạm Minh',  40, 28, 'A102', 'Nhà A', 'lý thuyết', 'đang học'), -- section_id=3
(4, 3, 'LOG101-01', 'ThS. Nguyễn An', 35, 30, 'C301', 'Nhà C', 'lý thuyết', 'đang học'); -- section_id=4

-- ── enrollment ─────────────────────────────────────────────────────────────────
INSERT INTO `enrollment` (`user_id`, `course_id`, `semester_id`, `section_id`, `status`, `midterm_score`, `final_score`, `assignment_score`, `total_score`, `letter_grade`, `grade_point`, `is_passed`) VALUES
(2, 1, 1, 1, 'hoàn thành', 8.5, 9.0, 9.5, 9.0, 'A',  4.0, true),
(2, 2, 1, 2, 'hoàn thành', 7.0, 8.0, 8.5, 7.9, 'B+', 3.5, true),
(3, 1, 2, 3, 'hoàn thành', 6.5, 7.0, 7.5, 7.0, 'B',  3.0, true),
(4, 4, 3, 4, 'hoàn thành', 5.5, 6.0, 6.5, 6.0, 'C',  2.0, true),
(5, 1, 4, NULL, 'hoàn thành', 9.0, 9.5, 9.0, 9.2, 'A',  4.0, true),
(5, 2, 4, NULL, 'hoàn thành', 8.0, 8.5, 8.5, 8.3, 'B+', 3.5, true),
(5, 3, 4, NULL, 'đang học',   0,   0,   0,   0,   NULL, NULL, false);

-- ── schedule ───────────────────────────────────────────────────────────────────
-- Dùng section_id (FK -> class_section), KHÔNG còn course_id / semester_id trực tiếp
INSERT INTO `schedule` (`user_id`, `section_id`, `day_of_week`, `start_period`, `end_period`, `start_time`, `end_time`, `room`, `building`, `lecturer_name`, `week_start`, `week_end`, `schedule_type`) VALUES
(NULL, 1, 2, 1, 3, '07:00:00', '09:30:00', 'A101', 'Nhà A', 'TS. Phạm Minh',  1, 15, 1),
(NULL, 2, 4, 4, 6, '09:40:00', '12:00:00', 'B202', 'Nhà B', 'ThS. Lê Huy',    1, 15, 1),
(NULL, 3, 3, 1, 3, '07:00:00', '09:30:00', 'A102', 'Nhà A', 'TS. Phạm Minh',  1, 15, 1),
(NULL, 4, 5, 7, 9, '13:00:00', '15:30:00', 'C301', 'Nhà C', 'ThS. Nguyễn An', 1, 15, 1),
(5,    1, 2, 1, 3, '07:00:00', '09:30:00', 'A103', 'Nhà A', 'TS. Phạm Minh',  1, 15, 1),
(5,    2, 5, 4, 6, '09:40:00', '12:00:00', 'B201', 'Nhà B', 'ThS. Lê Huy',    1, 15, 1),
(5,    3, 6, 7, 9, '13:00:00', '15:30:00', 'C302', 'Nhà C', 'TS. Nguyễn Hoàng', 1, 15, 1);
-- ── fee ────────────────────────────────────────────────────────────────────────
INSERT INTO `fee` (`user_id`, `semester_id`, `total_amount`, `paid_amount`, `remaining_amount`, `due_date`, `status`, `payment_method`, `paid_at`) VALUES
(2, 1, 12000000, 12000000,       0, '2025-10-01', 'đã đóng đủ',    'chuyển khoản', '2025-09-15 10:00:00'),
(3, 2, 11500000,  5000000, 6500000, '2025-10-01', 'đóng một phần', 'tiền mặt',     '2025-09-20 09:00:00'),
(4, 3, 10000000,        0, 10000000,'2025-10-01', 'chưa đóng',     NULL,            '2025-09-01 00:00:00'),
(5, 4, 12500000, 12500000,       0, '2025-10-01', 'đã đóng đủ',    'chuyển khoản', '2025-09-10 08:30:00');

-- ── dormitory_room ─────────────────────────────────────────────────────────────
INSERT INTO `dormitory_room` (`room_code`, `building`, `floor`, `capacity`, `current_occupancy`, `room_type`, `price_per_month`, `status`, `amenities`) VALUES
('A-201', 'KTX A', 2, 4, 3, 'thường',   800000, 'còn chỗ', 'Wifi, Máy lạnh'),
('B-305', 'KTX B', 3, 6, 6, 'dịch vụ', 1200000, 'đã đầy',  'Wifi, Máy lạnh, Máy giặt');

-- ── dormitory_registration ─────────────────────────────────────────────────────
INSERT INTO `dormitory_registration` (`user_id`, `room_id`, `start_date`, `end_date`, `status`, `total_fee`, `paid_status`) VALUES
(2, 1, '2025-09-01', '2026-06-01', 'đã duyệt',  7200000, 'đã đóng'),
(3, 1, '2025-09-01', '2026-06-01', 'chờ duyệt', 7200000, 'chưa đóng'),
(5, 1, '2025-09-01', '2026-06-01', 'đã duyệt',  7200000, 'đã đóng');

-- ── curriculum ─────────────────────────────────────────────────────────────────
INSERT INTO `curriculum` (`major`, `academic_year`, `total_credits_required`, `description`) VALUES
('Kỹ thuật phần mềm', 'K65', 145, 'Chương trình đào tạo KTPM K65'),
('Logistics',         'K65', 140, 'Chương trình đào tạo Logistics K65');

-- ── curriculum_item ────────────────────────────────────────────────────────────
INSERT INTO `curriculum_item` (`curriculum_id`, `course_id`, `semester_suggestion`, `is_required`, `group_name`) VALUES
(1, 1, 1, true, 'Cơ sở ngành'),
(1, 2, 2, true, 'Cơ sở ngành'),
(1, 3, 3, true, 'Chuyên ngành'),
(2, 4, 1, true, 'Đại cương');

-- ── notification ───────────────────────────────────────────────────────────────
INSERT INTO `notification` (`user_id`, `title`, `body`, `type`, `related_entity_type`, `related_entity_id`, `is_read`, `scheduled_for`) VALUES
(2, 'Thông báo học phí',           'Bạn đã hoàn thành học phí học kỳ 1',          'học phí', 'fee',       1, true,  '2025-09-15 10:00:00'),
(3, 'Cảnh báo công nợ',            'Bạn còn nợ học phí',                          'học phí', 'fee',       2, false, '2025-09-20 10:00:00'),
(2, 'Lịch học thay đổi',           'Phòng học môn CSDL đổi sang B305',            'lịch học','schedule',  2, false, '2025-09-22 07:00:00'),
(5, 'Đăng ký môn học thành công',  'Bạn đã đăng ký thành công môn Lập trình Web', 'học tập', 'enrollment',5, false, '2025-09-05 09:00:00');

-- ── academic_warning ───────────────────────────────────────────────────────────
INSERT INTO `academic_warning` (`user_id`, `semester_id`, `warning_type`, `description`, `resolved_at`, `status`) VALUES
(4, 3, 'GPA thấp', 'Điểm GPA dưới mức yêu cầu', '2025-12-01 00:00:00', 'đang hiệu lực');

-- ── service_request ────────────────────────────────────────────────────────────
INSERT INTO `service_request` (`user_id`, `service_type`, `description`, `status`, `resolved_at`, `result_note`, `attachment_url`) VALUES
(2, 'Giấy xác nhận sinh viên', 'Xin cấp giấy xác nhận sinh viên',      'hoàn thành', '2025-09-18 10:00:00', 'Đã gửi file PDF qua email', 'https://cdn.utc2/files/xacnhan.pdf'),
(3, 'Cấp bảng điểm',           'Xin cấp bảng điểm tiếng Anh',         'chờ xử lý',  NULL,                  NULL,                        NULL),
(5, 'Xin cấp thẻ sinh viên',   'Xin cấp lại thẻ sinh viên do bị mất', 'đang xử lý', NULL,                  NULL,                        NULL);

-- ── feedback ───────────────────────────────────────────────────────────────────
INSERT INTO `feedback` (`user_id`, `type`, `content`, `status`, `admin_reply`) VALUES
(2, 'góp ý',               'Hệ thống hoạt động khá tốt',               'đã phản hồi', 'Cảm ơn bạn đã góp ý'),
(3, 'báo cáo lỗi hệ thống','Không xem được thời khóa biểu',            'chưa đọc',    NULL),
(5, 'góp ý',               'Giao diện ứng dụng dễ sử dụng và trực quan', 'đã phản hồi', 'Cảm ơn bạn đã đóng góp ý kiến');
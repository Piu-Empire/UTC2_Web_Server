-- =========================================================
-- BỔ SUNG SINH VIÊN: CAO TRUNG TINH - MSSV 6551071085
-- File riêng để import thêm dữ liệu
-- =========================================================

-- user
INSERT INTO `user`
(`email`, `password_hash`, `auth_provider`)
VALUES
    ('6551071085@st.utc2.edu.vn', 'hashed_sv_tinh_1085', 'local');

-- user_profile
INSERT INTO `user_profile`
(`user_id`, `full_name`, `phone_number`, `avatar_url`, `date_of_birth`, `gender`)
VALUES
    (5, 'Cao Trung Tinh', '0944444444', 'https://cdn.utc2/avatar/sv_tinh.png', '2006-10-18', 'Nam');

-- student_profile
INSERT INTO `student_profile`
(`user_id`, `student_code`, `faculty`, `advisor_id`, `major`, `academic_year`, `class_name`, `status`)
VALUES
    (5, '6551071085', 'Công nghệ thông tin', 1, 'Kỹ thuật phần mềm', 'K65', '65TH3', 'đang học');

-- semester
INSERT INTO `semester`
(`user_id`, `semester_name`, `academic_year`, `semester_number`,
 `start_date`, `end_date`, `gpa`, `total_credits`, `passed_credits`)
VALUES
    (5, 'Học kỳ 1 năm 2025-2026', '2025-2026', 1,
     '2025-09-01', '2026-01-15', 3.75, 19, 19);

-- enrollment
INSERT INTO `enrollment`
(`user_id`, `course_id`, `semester_id`, `status`,
 `midterm_score`, `final_score`, `assignment_score`,
 `total_score`, `letter_grade`, `grade_point`, `is_passed`)
VALUES
    (5, 1, 4, 'hoàn thành', 9.0, 9.5, 9.0, 9.2, 'A', 4.0, true),
    (5, 2, 4, 'hoàn thành', 8.0, 8.5, 8.5, 8.3, 'B+', 3.5, true),
    (5, 3, 4, 'đang học', 0, 0, 0, 0, NULL, NULL, false);

-- schedule
INSERT INTO `schedule`
(`user_id`, `course_id`, `semester_id`,
 `day_of_week`, `start_period`, `end_period`,
 `start_time`, `end_time`,
 `room`, `building`, `lecturer_name`,
 `week_start`, `week_end`)
VALUES
    (5, 1, 4, 2, 1, 3, '07:00:00', '09:30:00',
     'A103', 'Nhà A', 'TS. Phạm Minh', 1, 15),

    (5, 2, 4, 5, 4, 6, '09:40:00', '12:00:00',
     'B201', 'Nhà B', 'ThS. Lê Huy', 1, 15),

    (5, 3, 4, 6, 7, 9, '13:00:00', '15:30:00',
     'C302', 'Nhà C', 'TS. Nguyễn Hoàng', 1, 15);

-- fee
INSERT INTO `fee`
(`user_id`, `semester_id`,
 `total_amount`, `paid_amount`, `remaining_amount`,
 `due_date`, `status`, `payment_method`, `paid_at`)
VALUES
    (5, 4, 12500000, 12500000, 0,
     '2025-10-01', 'đã đóng đủ',
     'chuyển khoản', '2025-09-10 08:30:00');

-- dormitory_registration
INSERT INTO `dormitory_registration`
(`user_id`, `room_id`,
 `start_date`, `end_date`,
 `status`, `total_fee`, `paid_status`)
VALUES
    (5, 1,
     '2025-09-01', '2026-06-01',
     'đã duyệt', 7200000, 'đã đóng');

-- notification
INSERT INTO `notification`
(`user_id`, `title`, `body`,
 `type`, `related_entity_type`,
 `related_entity_id`, `is_read`,
 `scheduled_for`)
VALUES
    (5,
     'Đăng ký môn học thành công',
     'Bạn đã đăng ký thành công môn Lập trình Web',
     'học tập',
     'enrollment',
     5,
     false,
     '2025-09-05 09:00:00');

-- service_request
INSERT INTO `service_request`
(`user_id`, `service_type`,
 `description`, `status`,
 `resolved_at`, `result_note`,
 `attachment_url`)
VALUES
    (5,
     'Xin cấp thẻ sinh viên',
     'Xin cấp lại thẻ sinh viên do bị mất',
     'đang xử lý',
     NULL,
     NULL,
     NULL);

-- feedback
INSERT INTO `feedback`
(`user_id`, `type`,
 `content`, `status`,
 `admin_reply`)
VALUES
    (5,
     'góp ý',
     'Giao diện ứng dụng dễ sử dụng và trực quan',
     'đã phản hồi',
     'Cảm ơn bạn đã đóng góp ý kiến');
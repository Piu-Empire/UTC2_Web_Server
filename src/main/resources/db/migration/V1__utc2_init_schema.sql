
CREATE TABLE IF NOT EXISTS `USER` (
    `user_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính, ID người dùng',
    `email` VARCHAR(255) UNIQUE COMMENT 'Email dùng để đăng nhập',
    `password_hash` VARCHAR(255) COMMENT 'Mật khẩu đã được mã hóa',
    `auth_provider` VARCHAR(50) COMMENT 'Nền tảng xác thực (VD: local, google, microsoft)',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm tạo tài khoản',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm cập nhật thông tin cuối cùng',
    PRIMARY KEY(`user_id`)
);

-- Bảng thông tin danh sách các cố vấn học tập
CREATE TABLE IF NOT EXISTS `ADVISOR` (
    `advisor_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính, ID cố vấn',
    `full_name` VARCHAR(255) COMMENT 'Họ và tên Cố vấn học tập',
    `email` VARCHAR(255) UNIQUE COMMENT 'Email liên hệ công việc',
    `phone` VARCHAR(20) COMMENT 'Số điện thoại liên hệ',
    `faculty` VARCHAR(100) COMMENT 'Khoa trực thuộc',
    `office_room` VARCHAR(50) COMMENT 'Phòng làm việc',
    PRIMARY KEY(`advisor_id`)
);

-- Bảng lưu trữ hồ sơ cá nhân chung của người dùng (có thể là sinh viên, giảng viên, admin)
CREATE TABLE IF NOT EXISTS `USER_PROFILE` (
    `user_id` BIGINT NOT NULL UNIQUE COMMENT 'Khóa ngoại -> USER',
    `full_name` VARCHAR(255) COMMENT 'Họ và tên đầy đủ',
    `phone_number` VARCHAR(20) COMMENT 'Số điện thoại cá nhân',
    `avatar_url` TEXT COMMENT 'Đường dẫn tới ảnh đại diện',
    `date_of_birth` DATE COMMENT 'Ngày tháng năm sinh',
    `gender` VARCHAR(20) COMMENT 'Giới tính',
    PRIMARY KEY(`user_id`)
);

-- Bảng lưu trữ hồ sơ học tập chuyên biệt dành cho sinh viên
CREATE TABLE IF NOT EXISTS `STUDENT_PROFILE` (
    `user_id` BIGINT NOT NULL UNIQUE COMMENT 'Khóa ngoại -> USER',
    `student_code` VARCHAR(50) UNIQUE COMMENT 'Mã số sinh viên',
    `faculty` VARCHAR(100) COMMENT 'Khoa đang theo học',
    `advisor_id` BIGINT COMMENT 'Khóa ngoại -> ADVISOR',
    `major` VARCHAR(100) COMMENT 'Chuyên ngành học',
    `academic_year` VARCHAR(50) COMMENT 'Khóa học (VD: K65, K66)',
    `class_name` VARCHAR(50) COMMENT 'Lớp sinh hoạt định kỳ',
    `status` VARCHAR(50) COMMENT 'Trạng thái học tập (đang học, bảo lưu, thôi học, đã tốt nghiệp)',
    PRIMARY KEY(`user_id`)
);

-- Bảng quản lý kết quả học tập tổng quan theo từng học kỳ của sinh viên
CREATE TABLE IF NOT EXISTS `SEMESTER` (
    `semester_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> USER',
    `semester_name` VARCHAR(255) COMMENT 'Tên gọi học kỳ (VD: Học kỳ 1 năm 2023-2024)',
    `academic_year` VARCHAR(50) COMMENT 'Năm học tương ứng',
    `semester_number` INTEGER COMMENT 'Thứ tự học kỳ (1, 2, 3...)',
    `start_date` DATE COMMENT 'Ngày bắt đầu học kỳ',
    `end_date` DATE COMMENT 'Ngày kết thúc học kỳ',
    `gpa` DECIMAL(4,2) COMMENT 'Điểm trung bình tích lũy của học kỳ đó (hệ 4.0)',
    `total_credits` INTEGER COMMENT 'Tổng số tín chỉ đã đăng ký trong kỳ',
    `passed_credits` INTEGER COMMENT 'Tổng số tín chỉ đã thi đạt trong kỳ',
    PRIMARY KEY(`semester_id`)
);

-- Bảng danh mục các môn học/học phần được giảng dạy
CREATE TABLE IF NOT EXISTS `COURSE` (
    `course_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `course_code` VARCHAR(50) UNIQUE COMMENT 'Mã học phần (VD: MATH101)',
    `course_name` VARCHAR(255) COMMENT 'Tên môn học',
    `credits` INTEGER COMMENT 'Số tín chỉ',
    `theory_hours` INTEGER COMMENT 'Số tiết lý thuyết',
    `practice_hours` INTEGER COMMENT 'Số tiết thực hành',
    `department` VARCHAR(100) COMMENT 'Khoa / Bộ môn phụ trách',
    `description` TEXT COMMENT 'Mô tả nội dung môn học',
    PRIMARY KEY(`course_id`)
);

-- Bảng đăng ký môn học và lưu trữ điểm số chi tiết của sinh viên
CREATE TABLE IF NOT EXISTS `ENROLLMENT` (
    `enrollment_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> USER',
    `course_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> COURSE',
    `semester_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> SEMESTER',
    `registered_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm đăng ký',
    `status` VARCHAR(50) COMMENT 'Trạng thái: đã đăng ký / đã huỷ / hoàn thành',
    `midterm_score` DECIMAL(4,2) COMMENT 'Điểm giữa kỳ',
    `final_score` DECIMAL(4,2) COMMENT 'Điểm cuối kỳ',
    `assignment_score` DECIMAL(4,2) COMMENT 'Điểm quá trình/bài tập',
    `total_score` DECIMAL(4,2) COMMENT 'Điểm tổng kết môn',
    `letter_grade` VARCHAR(5) COMMENT 'Điểm chữ (A, B+, B, C, D, F)',
    `grade_point` DECIMAL(3,2) COMMENT 'Điểm quy đổi hệ 4.0',
    `is_passed` BOOLEAN COMMENT '1: qua môn, 0: rớt',
    PRIMARY KEY(`enrollment_id`)
);

-- Bảng thời khóa biểu cho từng môn học trong kỳ
CREATE TABLE IF NOT EXISTS `SCHEDULE` (
    `schedule_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> USER',
    `course_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> COURSE',
    `semester_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> SEMESTER',
    `day_of_week` INTEGER COMMENT 'Thứ trong tuần (2-8)',
    `start_period` INTEGER COMMENT 'Tiết bắt đầu',
    `end_period` INTEGER COMMENT 'Tiết kết thúc',
    `start_time` TIME COMMENT 'Giờ bắt đầu',
    `end_time` TIME COMMENT 'Giờ kết thúc',
    `room` VARCHAR(50) COMMENT 'Phòng học',
    `building` VARCHAR(100) COMMENT 'Tòa nhà',
    `lecturer_name` VARCHAR(255) COMMENT 'Tên giảng viên',
    `week_start` INTEGER COMMENT 'Tuần bắt đầu áp dụng',
    `week_end` INTEGER COMMENT 'Tuần kết thúc áp dụng',
    PRIMARY KEY(`schedule_id`)
);

-- Bảng quản lý học phí và các khoản thu của sinh viên
CREATE TABLE IF NOT EXISTS `FEE` (
    `fee_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> USER',
    `semester_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> SEMESTER',
    `total_amount` DECIMAL(15,2) COMMENT 'Tổng học phí phải đóng',
    `paid_amount` DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Số tiền đã đóng',
    `remaining_amount` DECIMAL(15,2) COMMENT 'Số tiền còn nợ',
    `due_date` DATE COMMENT 'Hạn đóng học phí',
    `status` VARCHAR(50) COMMENT 'Trạng thái: chưa đóng / đóng một phần / đã đóng đủ',
    `payment_method` VARCHAR(50) COMMENT 'Hình thức thanh toán (chuyển khoản, tiền mặt...)',
    `paid_at` TIMESTAMP NOT NULL COMMENT 'Thời điểm thanh toán (NULL nếu chưa đóng)',
    PRIMARY KEY(`fee_id`)
);

-- Bảng danh sách các phòng trong ký túc xá
CREATE TABLE IF NOT EXISTS `DORMITORY_ROOM` (
    `room_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `room_code` VARCHAR(50) UNIQUE COMMENT 'Mã phòng (VD: A-201)',
    `building` VARCHAR(100) COMMENT 'Tòa nhà',
    `floor` INTEGER COMMENT 'Tầng',
    `capacity` INTEGER COMMENT 'Sức chứa tối đa',
    `current_occupancy` INTEGER DEFAULT 0 COMMENT 'Số SV đang ở',
    `room_type` VARCHAR(50) COMMENT 'Loại phòng (thường, dịch vụ...)',
    `price_per_month` DECIMAL(15,2) COMMENT 'Giá thuê mỗi tháng',
    `status` VARCHAR(50) COMMENT 'Trạng thái: còn chỗ / đã đầy / đang bảo trì',
    `amenities` TEXT COMMENT 'Cơ sở vật chất/Tiện nghi có sẵn',
    PRIMARY KEY(`room_id`)
);

-- Bảng quản lý việc đăng ký lưu trú ký túc xá của sinh viên
CREATE TABLE IF NOT EXISTS `DORMITORY_REGISTRATION` (
    `dorm_reg_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> USER',
    `room_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> DORMITORY_ROOM',
    `registered_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm đăng ký',
    `start_date` DATE COMMENT 'Ngày chuyển vào',
    `end_date` DATE COMMENT 'Ngày trả phòng dự kiến',
    `status` VARCHAR(50) COMMENT 'Trạng thái: chờ duyệt / đã duyệt / từ chối / đã trả phòng',
    `total_fee` DECIMAL(15,2) COMMENT 'Tổng tiền KTX',
    `paid_status` VARCHAR(50) COMMENT 'Tình trạng thanh toán: chưa đóng / đã đóng',
    PRIMARY KEY(`dorm_reg_id`)
);

-- Bảng quản lý chương trình đào tạo khung chuẩn
CREATE TABLE IF NOT EXISTS `CURRICULUM` (
    `curriculum_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `major` VARCHAR(100) COMMENT 'Ngành học áp dụng',
    `academic_year` VARCHAR(50) COMMENT 'Khóa áp dụng (VD: K65)',
    `total_credits_required` INTEGER COMMENT 'Tín chỉ tối thiểu để tốt nghiệp',
    `description` TEXT COMMENT 'Mô tả / mục tiêu đào tạo',
    PRIMARY KEY(`curriculum_id`)
);

-- Bảng chi tiết các môn học cấu thành nên chương trình đào tạo khung
CREATE TABLE IF NOT EXISTS `CURRICULUM_ITEM` (
    `curriculum_item_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `curriculum_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> CURRICULUM',
    `course_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> COURSE',
    `semester_suggestion` INTEGER COMMENT 'Học kỳ gợi ý đăng ký',
    `is_required` BOOLEAN COMMENT '1: Môn bắt buộc, 0: Môn tự chọn',
    `group_name` VARCHAR(100) COMMENT 'Nhóm môn (Đại cương, Cơ sở ngành, Chuyên ngành...)',
    PRIMARY KEY(`curriculum_item_id`)
);

-- Bảng lưu trữ thông báo gửi đến người dùng
CREATE TABLE IF NOT EXISTS `NOTIFICATION` (
    `notification_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> USER',
    `title` VARCHAR(255) COMMENT 'Tiêu đề thông báo',
    `body` TEXT COMMENT 'Nội dung chi tiết thông báo',
    `type` VARCHAR(50) COMMENT 'Phân loại: lịch học / học phí / cảnh báo / hệ thống',
    `related_entity_type` VARCHAR(50) COMMENT 'Tên bảng liên quan (để tạo link/điều hướng)',
    `related_entity_id` BIGINT COMMENT 'ID bản ghi liên quan',
    `is_read` BOOLEAN DEFAULT false COMMENT 'Trạng thái đọc (1: đã đọc, 0: chưa đọc)',
    `sent_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm gửi thông báo',
    `scheduled_for` TIMESTAMP NOT NULL COMMENT 'Thời điểm dự kiến phát (NULL = phát ngay lập tức)',
    PRIMARY KEY(`notification_id`)
);

-- Bảng ghi nhận các vi phạm, cảnh cáo học vụ của sinh viên
CREATE TABLE IF NOT EXISTS `ACADEMIC_WARNING` (
    `warning_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> USER',
    `semester_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> SEMESTER',
    `warning_type` VARCHAR(100) COMMENT 'Phân loại: cấm thi / nợ học phí / GPA thấp / vắng nhiều',
    `description` TEXT COMMENT 'Mô tả chi tiết và hình thức xử lý',
    `issued_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm phát hành cảnh báo',
    `resolved_at` TIMESTAMP NOT NULL COMMENT 'Thời điểm giải quyết xong (NULL nếu chưa xong)',
    `status` VARCHAR(50) COMMENT 'Trạng thái: đang hiệu lực / đã giải quyết / đã hủy',
    PRIMARY KEY(`warning_id`)
);

-- Bảng quản lý các yêu cầu hỗ trợ, dịch vụ một cửa từ sinh viên
CREATE TABLE IF NOT EXISTS `SERVICE_REQUEST` (
    `request_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> USER',
    `service_type` VARCHAR(100) COMMENT 'Loại yêu cầu: cấp bảng điểm / giấy xác nhận SV / làm lại thẻ SV...',
    `description` TEXT COMMENT 'Nội dung chi tiết yêu cầu',
    `status` VARCHAR(50) DEFAULT 'chờ xử lý' COMMENT 'Trạng thái: chờ xử lý / đang xử lý / hoàn thành / từ chối',
    `submitted_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm nộp yêu cầu',
    `resolved_at` TIMESTAMP NOT NULL COMMENT 'Thời điểm xử lý xong (NULL nếu chưa xong)',
    `result_note` TEXT COMMENT 'Ghi chú về kết quả hỗ trợ hoặc lý do từ chối',
    `attachment_url` TEXT COMMENT 'Đường dẫn file đính kèm nếu có',
    PRIMARY KEY(`request_id`)
);

-- Bảng lưu trữ phản hồi, góp ý của người dùng với nhà trường/hệ thống
CREATE TABLE IF NOT EXISTS `FEEDBACK` (
    `feedback_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> USER',
    `type` VARCHAR(50) COMMENT 'Phân loại: góp ý / liên hệ / báo cáo lỗi hệ thống',
    `content` TEXT COMMENT 'Nội dung phản hồi',
    `submitted_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm gửi phản hồi',
    `status` VARCHAR(50) DEFAULT 'chưa đọc' COMMENT 'Trạng thái: chưa đọc / đã đọc / đã phản hồi',
    `admin_reply` TEXT COMMENT 'Câu trả lời từ quản trị viên (NULL nếu chưa phản hồi)',
    PRIMARY KEY(`feedback_id`)
);

-- -----------------------------------------------------
-- PHẦN KHAI BÁO CÁC KHÓA NGOẠI (FOREIGN KEYS)
-- -----------------------------------------------------

ALTER TABLE `USER_PROFILE`
ADD FOREIGN KEY(`user_id`) REFERENCES `USER`(`user_id`)
ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `STUDENT_PROFILE`
ADD FOREIGN KEY(`user_id`) REFERENCES `USER`(`user_id`)
ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `STUDENT_PROFILE`
ADD FOREIGN KEY(`advisor_id`) REFERENCES `ADVISOR`(`advisor_id`)
ON UPDATE NO ACTION ON DELETE SET NULL;

ALTER TABLE `SEMESTER`
ADD FOREIGN KEY(`user_id`) REFERENCES `USER`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `ENROLLMENT`
ADD FOREIGN KEY(`user_id`) REFERENCES `USER`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `ENROLLMENT`
ADD FOREIGN KEY(`course_id`) REFERENCES `COURSE`(`course_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `ENROLLMENT`
ADD FOREIGN KEY(`semester_id`) REFERENCES `SEMESTER`(`semester_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `SCHEDULE`
ADD FOREIGN KEY(`user_id`) REFERENCES `USER`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `SCHEDULE`
ADD FOREIGN KEY(`course_id`) REFERENCES `COURSE`(`course_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `SCHEDULE`
ADD FOREIGN KEY(`semester_id`) REFERENCES `SEMESTER`(`semester_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `FEE`
ADD FOREIGN KEY(`user_id`) REFERENCES `USER`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `FEE`
ADD FOREIGN KEY(`semester_id`) REFERENCES `SEMESTER`(`semester_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `DORMITORY_REGISTRATION`
ADD FOREIGN KEY(`user_id`) REFERENCES `USER`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `DORMITORY_REGISTRATION`
ADD FOREIGN KEY(`room_id`) REFERENCES `DORMITORY_ROOM`(`room_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `CURRICULUM_ITEM`
ADD FOREIGN KEY(`curriculum_id`) REFERENCES `CURRICULUM`(`curriculum_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `CURRICULUM_ITEM`
ADD FOREIGN KEY(`course_id`) REFERENCES `COURSE`(`course_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `NOTIFICATION`
ADD FOREIGN KEY(`user_id`) REFERENCES `USER`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `ACADEMIC_WARNING`
ADD FOREIGN KEY(`user_id`) REFERENCES `USER`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `ACADEMIC_WARNING`
ADD FOREIGN KEY(`semester_id`) REFERENCES `SEMESTER`(`semester_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `SERVICE_REQUEST`
ADD FOREIGN KEY(`user_id`) REFERENCES `USER`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `FEEDBACK`
ADD FOREIGN KEY(`user_id`) REFERENCES `USER`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;


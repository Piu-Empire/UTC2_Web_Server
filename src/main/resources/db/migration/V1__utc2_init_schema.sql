CREATE TABLE IF NOT EXISTS `user` (
    `user_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính, ID người dùng',
    `email` VARCHAR(255) UNIQUE COMMENT 'Email dùng để đăng nhập',
    `password_hash` VARCHAR(255) COMMENT 'Mật khẩu đã được mã hóa',
    `auth_provider` VARCHAR(50) COMMENT 'Nền tảng xác thực (VD: local, google, microsoft)',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm tạo tài khoản',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm cập nhật thông tin cuối cùng',
    PRIMARY KEY(`user_id`)
);

CREATE TABLE IF NOT EXISTS `advisor` (
    `advisor_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính, ID cố vấn',
    `full_name` VARCHAR(255) COMMENT 'Họ và tên Cố vấn học tập',
    `email` VARCHAR(255) UNIQUE COMMENT 'Email liên hệ công việc',
    `phone` VARCHAR(20) COMMENT 'Số điện thoại liên hệ',
    `faculty` VARCHAR(100) COMMENT 'Khoa trực thuộc',
    `office_room` VARCHAR(50) COMMENT 'Phòng làm việc',
    PRIMARY KEY(`advisor_id`)
);

CREATE TABLE IF NOT EXISTS `user_profile` (
    `user_id` BIGINT NOT NULL UNIQUE COMMENT 'Khóa ngoại -> user',
    `full_name` VARCHAR(255) COMMENT 'Họ và tên đầy đủ',
    `phone_number` VARCHAR(20) COMMENT 'Số điện thoại cá nhân',
    `avatar_url` TEXT COMMENT 'Đường dẫn tới ảnh đại diện',
    `date_of_birth` DATE COMMENT 'Ngày tháng năm sinh',
    `gender` VARCHAR(20) COMMENT 'Giới tính',
    PRIMARY KEY(`user_id`)
);

CREATE TABLE IF NOT EXISTS `student_profile` (
    `user_id` BIGINT NOT NULL UNIQUE COMMENT 'Khóa ngoại -> user',
    `student_code` VARCHAR(50) UNIQUE COMMENT 'Mã số sinh viên',
    `faculty` VARCHAR(100) COMMENT 'Khoa đang theo học',
    `advisor_id` BIGINT COMMENT 'Khóa ngoại -> advisor',
    `major` VARCHAR(100) COMMENT 'Chuyên ngành học',
    `academic_year` VARCHAR(50) COMMENT 'Khóa học (VD: K65, K66)',
    `class_name` VARCHAR(50) COMMENT 'Lớp sinh hoạt định kỳ',
    `status` VARCHAR(50) COMMENT 'Trạng thái học tập',
    PRIMARY KEY(`user_id`)
);

CREATE TABLE IF NOT EXISTS `semester` (
    `semester_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> user',
    `semester_name` VARCHAR(255),
    `academic_year` VARCHAR(50),
    `semester_number` INTEGER,
    `start_date` DATE,
    `end_date` DATE,
    `gpa` DECIMAL(4,2),
    `total_credits` INTEGER,
    `passed_credits` INTEGER,
    PRIMARY KEY(`semester_id`)
);

CREATE TABLE IF NOT EXISTS `course` (
    `course_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `course_code` VARCHAR(50) UNIQUE,
    `course_name` VARCHAR(255),
    `credits` INTEGER,
    `theory_hours` INTEGER,
    `practice_hours` INTEGER,
    `department` VARCHAR(100),
    `description` TEXT,
    PRIMARY KEY(`course_id`)
);

CREATE TABLE IF NOT EXISTS `enrollment` (
    `enrollment_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> user',
    `course_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> course',
    `semester_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> semester',
    `registered_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `status` VARCHAR(50),
    `midterm_score` DECIMAL(4,2),
    `final_score` DECIMAL(4,2),
    `assignment_score` DECIMAL(4,2),
    `total_score` DECIMAL(4,2),
    `letter_grade` VARCHAR(5),
    `grade_point` DECIMAL(3,2),
    `is_passed` BOOLEAN,
    PRIMARY KEY(`enrollment_id`)
);

CREATE TABLE IF NOT EXISTS `schedule` (
    `schedule_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> user',
    `course_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> course',
    `semester_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> semester',
    `day_of_week` INTEGER,
    `start_period` INTEGER,
    `end_period` INTEGER,
    `start_time` TIME,
    `end_time` TIME,
    `room` VARCHAR(50),
    `building` VARCHAR(100),
    `lecturer_name` VARCHAR(255),
    `week_start` INTEGER,
    `week_end` INTEGER,
    PRIMARY KEY(`schedule_id`)
);

CREATE TABLE IF NOT EXISTS `fee` (
    `fee_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> user',
    `semester_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> semester',
    `total_amount` DECIMAL(15,2),
    `paid_amount` DECIMAL(15,2) DEFAULT 0.00,
    `remaining_amount` DECIMAL(15,2),
    `due_date` DATE,
    `status` VARCHAR(50),
    `payment_method` VARCHAR(50),
    `paid_at` TIMESTAMP NULL,
    PRIMARY KEY(`fee_id`)
);

CREATE TABLE IF NOT EXISTS `dormitory_room` (
    `room_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `room_code` VARCHAR(50) UNIQUE,
    `building` VARCHAR(100),
    `floor` INTEGER,
    `capacity` INTEGER,
    `current_occupancy` INTEGER DEFAULT 0,
    `room_type` VARCHAR(50),
    `price_per_month` DECIMAL(15,2),
    `status` VARCHAR(50),
    `amenities` TEXT,
    PRIMARY KEY(`room_id`)
);

CREATE TABLE IF NOT EXISTS `dormitory_registration` (
    `dorm_reg_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> user',
    `room_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> dormitory_room',
    `registered_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `start_date` DATE,
    `end_date` DATE,
    `status` VARCHAR(50),
    `total_fee` DECIMAL(15,2),
    `paid_status` VARCHAR(50),
    PRIMARY KEY(`dorm_reg_id`)
);

CREATE TABLE IF NOT EXISTS `curriculum` (
    `curriculum_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `major` VARCHAR(100),
    `academic_year` VARCHAR(50),
    `total_credits_required` INTEGER,
    `description` TEXT,
    PRIMARY KEY(`curriculum_id`)
);

CREATE TABLE IF NOT EXISTS `curriculum_item` (
    `curriculum_item_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `curriculum_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> curriculum',
    `course_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> course',
    `semester_suggestion` INTEGER,
    `is_required` BOOLEAN,
    `group_name` VARCHAR(100),
    PRIMARY KEY(`curriculum_item_id`)
);

CREATE TABLE IF NOT EXISTS `notification` (
    `notification_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> user',
    `title` VARCHAR(255),
    `body` TEXT,
    `type` VARCHAR(50),
    `related_entity_type` VARCHAR(50),
    `related_entity_id` BIGINT,
    `is_read` BOOLEAN DEFAULT false,
    `sent_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `scheduled_for` TIMESTAMP NULL,
    PRIMARY KEY(`notification_id`)
);

CREATE TABLE IF NOT EXISTS `academic_warning` (
    `warning_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> user',
    `semester_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> semester',
    `warning_type` VARCHAR(100),
    `description` TEXT,
    `issued_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `resolved_at` TIMESTAMP NULL,
    `status` VARCHAR(50),
    PRIMARY KEY(`warning_id`)
);

CREATE TABLE IF NOT EXISTS `service_request` (
    `request_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> user',
    `service_type` VARCHAR(100),
    `description` TEXT,
    `status` VARCHAR(50) DEFAULT 'chờ xử lý',
    `submitted_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `resolved_at` TIMESTAMP NULL,
    `result_note` TEXT,
    `attachment_url` TEXT,
    PRIMARY KEY(`request_id`)
);

CREATE TABLE IF NOT EXISTS `feedback` (
    `feedback_id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id` BIGINT NOT NULL COMMENT 'Khóa ngoại -> user',
    `type` VARCHAR(50),
    `content` TEXT,
    `submitted_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `status` VARCHAR(50) DEFAULT 'chưa đọc',
    `admin_reply` TEXT,
    PRIMARY KEY(`feedback_id`)
);

-- FOREIGN KEYS

ALTER TABLE `user_profile`
ADD FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`)
ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `student_profile`
ADD FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`)
ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `student_profile`
ADD FOREIGN KEY(`advisor_id`) REFERENCES `advisor`(`advisor_id`)
ON UPDATE NO ACTION ON DELETE SET NULL;

ALTER TABLE `semester`
ADD FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `enrollment`
ADD FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `enrollment`
ADD FOREIGN KEY(`course_id`) REFERENCES `course`(`course_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `enrollment`
ADD FOREIGN KEY(`semester_id`) REFERENCES `semester`(`semester_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `schedule`
ADD FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `schedule`
ADD FOREIGN KEY(`course_id`) REFERENCES `course`(`course_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `schedule`
ADD FOREIGN KEY(`semester_id`) REFERENCES `semester`(`semester_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `fee`
ADD FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `fee`
ADD FOREIGN KEY(`semester_id`) REFERENCES `semester`(`semester_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `dormitory_registration`
ADD FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `dormitory_registration`
ADD FOREIGN KEY(`room_id`) REFERENCES `dormitory_room`(`room_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `curriculum_item`
ADD FOREIGN KEY(`curriculum_id`) REFERENCES `curriculum`(`curriculum_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `curriculum_item`
ADD FOREIGN KEY(`course_id`) REFERENCES `course`(`course_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `notification`
ADD FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `academic_warning`
ADD FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `academic_warning`
ADD FOREIGN KEY(`semester_id`) REFERENCES `semester`(`semester_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `service_request`
ADD FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `feedback`
ADD FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`)
ON UPDATE NO ACTION ON DELETE NO ACTION;
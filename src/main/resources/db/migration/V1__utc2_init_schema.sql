CREATE TABLE IF NOT EXISTS `user` (
    `user_id`       BIGINT       NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `email`         VARCHAR(255) UNIQUE                         COMMENT 'Email đăng nhập',
    `password_hash` VARCHAR(255)                                COMMENT 'Mật khẩu đã mã hóa',
    `auth_provider` VARCHAR(50)                                 COMMENT 'local | google | microsoft',
    `created_at`    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`)
);

CREATE TABLE IF NOT EXISTS `advisor` (
    `advisor_id`  BIGINT       NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `full_name`   VARCHAR(255)                                COMMENT 'Họ và tên',
    `email`       VARCHAR(255) UNIQUE                         COMMENT 'Email liên hệ',
    `phone`       VARCHAR(20)                                 COMMENT 'Số điện thoại',
    `faculty`     VARCHAR(100)                                COMMENT 'Khoa trực thuộc',
    `office_room` VARCHAR(50)                                 COMMENT 'Phòng làm việc',
    PRIMARY KEY (`advisor_id`)
);

CREATE TABLE IF NOT EXISTS `user_profile` (
    `user_id`      BIGINT       NOT NULL UNIQUE COMMENT 'Khóa ngoại -> user',
    `full_name`    VARCHAR(255)               COMMENT 'Họ và tên đầy đủ',
    `phone_number` VARCHAR(20)                COMMENT 'Số điện thoại',
    `avatar_url`   TEXT                       COMMENT 'URL ảnh đại diện',
    `date_of_birth` DATE                      COMMENT 'Ngày sinh',
    `gender`       VARCHAR(20)                COMMENT 'Giới tính',
    PRIMARY KEY (`user_id`)
);

CREATE TABLE IF NOT EXISTS `student_profile` (
    `user_id`       BIGINT      NOT NULL UNIQUE COMMENT 'Khóa ngoại -> user',
    `student_code`  VARCHAR(50) UNIQUE          COMMENT 'Mã số sinh viên',
    `faculty`       VARCHAR(100)                COMMENT 'Khoa',
    `advisor_id`    BIGINT                      COMMENT 'Khóa ngoại -> advisor',
    `major`         VARCHAR(100)                COMMENT 'Chuyên ngành',
    `academic_year` VARCHAR(50)                 COMMENT 'Khóa học (VD: K65)',
    `class_name`    VARCHAR(50)                 COMMENT 'Lớp sinh hoạt',
    `status`        VARCHAR(50)                 COMMENT 'Trạng thái học tập',
    PRIMARY KEY (`user_id`)
);

CREATE TABLE IF NOT EXISTS `semester` (
    `semester_id`     BIGINT       NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id`         BIGINT       NOT NULL COMMENT 'Khóa ngoại -> user (người tạo / quản lý kỳ)',
    `semester_name`   VARCHAR(255)          COMMENT 'Tên học kỳ (VD: Học kỳ 1 năm 2025-2026)',
    `academic_year`   VARCHAR(50)           COMMENT 'Năm học (VD: 2025-2026)',
    `semester_number` INTEGER               COMMENT 'Thứ tự học kỳ (1, 2, 3...)',
    `start_date`      DATE                  COMMENT 'Ngày bắt đầu học kỳ',
    `end_date`        DATE                  COMMENT 'Ngày kết thúc học kỳ',
    `gpa`             DECIMAL(4,2)          COMMENT 'Điểm trung bình tích lũy (lưu cho SV)',
    `total_credits`   INTEGER               COMMENT 'Tổng số tín chỉ đã đăng ký trong kỳ',
    `passed_credits`  INTEGER               COMMENT 'Tổng số tín chỉ đã đạt trong kỳ',
    PRIMARY KEY (`semester_id`)
);

CREATE TABLE IF NOT EXISTS `course` (
    `course_id`      BIGINT       NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `course_code`    VARCHAR(50)  UNIQUE                         COMMENT 'Mã học phần (VD: INT101)',
    `course_name`    VARCHAR(255)                                COMMENT 'Tên môn học',
    `credits`        INTEGER                                     COMMENT 'Số tín chỉ',
    `theory_hours`   INTEGER                                     COMMENT 'Số tiết lý thuyết',
    `practice_hours` INTEGER                                     COMMENT 'Số tiết thực hành',
    `department`     VARCHAR(100)                                COMMENT 'Khoa / Bộ môn phụ trách',
    `description`    TEXT                                        COMMENT 'Mô tả nội dung môn học',
    PRIMARY KEY (`course_id`)
);

CREATE TABLE IF NOT EXISTS `class_section` (
    `section_id`          BIGINT       NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `course_id`           BIGINT       NOT NULL COMMENT 'Khóa ngoại -> course',
    `semester_id`         BIGINT       NOT NULL COMMENT 'Khóa ngoại -> semester',
    `section_code`        VARCHAR(50)           COMMENT 'Mã lớp học phần (VD: MATH101-01, MATH101-02)',
    `lecturer_name`       VARCHAR(255)          COMMENT 'Tên giảng viên phụ trách',
    `max_capacity`        INTEGER               COMMENT 'Sĩ số tối đa',
    `current_enrollment`  INTEGER  DEFAULT 0    COMMENT 'Số SV đã đăng ký',
    `room`                VARCHAR(50)           COMMENT 'Phòng học',
    `building`            VARCHAR(100)          COMMENT 'Tòa nhà',
    `section_type`        VARCHAR(50)           COMMENT 'Loại lớp: lý thuyết / thực hành / online',
    `status`              VARCHAR(50)           COMMENT 'Trạng thái: mở đăng ký / đóng / đã hủy',
    PRIMARY KEY (`section_id`)
);

CREATE TABLE IF NOT EXISTS `enrollment` (
    `enrollment_id`    BIGINT      NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id`          BIGINT      NOT NULL COMMENT 'Khóa ngoại -> user',
    `semester_id`      BIGINT      NOT NULL COMMENT 'Khóa ngoại -> semester',
    `course_id`        BIGINT      NOT NULL COMMENT 'Khóa ngoại -> course',
    `section_id`       BIGINT          NULL COMMENT 'Khóa ngoại -> class_section',
    `registered_at`    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm đăng ký',
    `status`           VARCHAR(50)          COMMENT 'Trạng thái: đã đăng ký / đã hủy / hoàn thành',
    `midterm_score`    DECIMAL(4,2)         COMMENT 'Điểm giữa kỳ',
    `final_score`      DECIMAL(4,2)         COMMENT 'Điểm cuối kỳ',
    `assignment_score` DECIMAL(4,2)         COMMENT 'Điểm quá trình/bài tập',
    `total_score`      DECIMAL(4,2)         COMMENT 'Điểm tổng kết môn',
    `letter_grade`     VARCHAR(5)           COMMENT 'Điểm chữ (A, B+, B, C, D, F)',
    `grade_point`      DECIMAL(3,2)         COMMENT 'Điểm quy đổi hệ 4.0',
    `is_passed`        BOOLEAN              COMMENT '1: qua môn, 0: rớt',
    PRIMARY KEY (`enrollment_id`)
);

CREATE TABLE IF NOT EXISTS `schedule` (
    `schedule_id`   BIGINT      NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id`       BIGINT          NULL COMMENT 'Khóa ngoại -> user (NULL = lịch chung môn học)',
    `day_of_week`   INTEGER          NULL COMMENT 'Thứ trong tuần (2-7=Thứ 2-7, 8=CN)',
    `start_period`  INTEGER          NULL COMMENT 'Tiết bắt đầu (1-12)',
    `end_period`    INTEGER          NULL COMMENT 'Tiết kết thúc (1-12)',
    `start_time`    TIME             NULL COMMENT 'Giờ bắt đầu',
    `end_time`      TIME             NULL COMMENT 'Giờ kết thúc',
    `room`          VARCHAR(50)      NULL COMMENT 'Phòng học',
    `building`      VARCHAR(100)     NULL COMMENT 'Tòa nhà',
    `lecturer_name` VARCHAR(255)     NULL COMMENT 'Tên giảng viên (denormalized)',
    `lecturer_id`   BIGINT           NULL COMMENT 'Khóa ngoại -> user (giảng viên)',
    `week_start`    INTEGER          NULL COMMENT 'Tuần bắt đầu áp dụng trong học kỳ',
    `week_end`      INTEGER          NULL COMMENT 'Tuần kết thúc áp dụng trong học kỳ',
    `schedule_type` INT          NOT NULL DEFAULT 1 COMMENT '1=Lịch học, 2=Lịch thi, 3=Lịch thi lại',
    `exam_date_start` DATE       NULL COMMENT 'Ngày thi/thi lại (bắt đầu)',
    `exam_date_end`   DATE       NULL COMMENT 'Ngày thi/thi lại (kết thúc)',
    `notes`         TEXT             NULL COMMENT 'Ghi chú',
    `updated_at`    TIMESTAMP        NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `section_id`    BIGINT      NOT NULL COMMENT 'Khóa ngoại -> class_section',
    PRIMARY KEY (`schedule_id`)
);

CREATE TABLE IF NOT EXISTS `fee` (
    `fee_id`           BIGINT        NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id`          BIGINT        NOT NULL COMMENT 'Khóa ngoại -> user',
    `semester_id`      BIGINT        NOT NULL COMMENT 'Khóa ngoại -> semester',
    `total_amount`     DECIMAL(15,2)          COMMENT 'Tổng học phí cần đóng',
    `paid_amount`      DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Số đã đóng',
    `remaining_amount` DECIMAL(15,2)          COMMENT 'Số còn lại',
    `due_date`         DATE                   COMMENT 'Hạn đóng',
    `status`           VARCHAR(50)            COMMENT 'đã đóng đủ / đóng một phần / chưa đóng',
    `payment_method`   VARCHAR(50)            COMMENT 'Hình thức thanh toán',
    `paid_at`          TIMESTAMP     NULL,
    PRIMARY KEY (`fee_id`)
);

CREATE TABLE IF NOT EXISTS `dormitory_room` (
    `room_id`           BIGINT        NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `room_code`         VARCHAR(50)   UNIQUE COMMENT 'Mã phòng',
    `building`          VARCHAR(100)         COMMENT 'Tòa KTX',
    `floor`             INTEGER              COMMENT 'Tầng',
    `capacity`          INTEGER              COMMENT 'Sức chứa tối đa',
    `current_occupancy` INTEGER       DEFAULT 0 COMMENT 'Số người hiện tại',
    `room_type`         VARCHAR(50)          COMMENT 'thường / dịch vụ',
    `price_per_month`   DECIMAL(15,2)        COMMENT 'Giá thuê mỗi tháng',
    `status`            VARCHAR(50)          COMMENT 'còn chỗ / đã đầy',
    `amenities`         TEXT                 COMMENT 'Tiện nghi',
    PRIMARY KEY (`room_id`)
);

CREATE TABLE IF NOT EXISTS `dormitory_registration` (
    `dorm_reg_id`   BIGINT        NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id`       BIGINT        NOT NULL COMMENT 'Khóa ngoại -> user',
    `room_id`       BIGINT        NOT NULL COMMENT 'Khóa ngoại -> dormitory_room',
    `registered_at` TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    `start_date`    DATE,
    `end_date`      DATE,
    `status`        VARCHAR(50)   COMMENT 'đã duyệt / chờ duyệt / từ chối',
    `total_fee`     DECIMAL(15,2),
    `paid_status`   VARCHAR(50)   COMMENT 'đã đóng / chưa đóng',
    PRIMARY KEY (`dorm_reg_id`)
);

CREATE TABLE IF NOT EXISTS `curriculum` (
    `curriculum_id`          BIGINT       NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `major`                  VARCHAR(100)          COMMENT 'Chuyên ngành',
    `academic_year`          VARCHAR(50)           COMMENT 'Khóa đào tạo',
    `total_credits_required` INTEGER               COMMENT 'Tổng tín chỉ yêu cầu',
    `description`            TEXT,
    PRIMARY KEY (`curriculum_id`)
);

CREATE TABLE IF NOT EXISTS `curriculum_item` (
    `curriculum_item_id` BIGINT      NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `curriculum_id`      BIGINT      NOT NULL COMMENT 'Khóa ngoại -> curriculum',
    `course_id`          BIGINT      NOT NULL COMMENT 'Khóa ngoại -> course',
    `semester_suggestion` INTEGER             COMMENT 'Học kỳ đề xuất học',
    `is_required`        BOOLEAN             COMMENT 'Bắt buộc hay tự chọn',
    `group_name`         VARCHAR(100)        COMMENT 'Nhóm môn học',
    PRIMARY KEY (`curriculum_item_id`)
);

CREATE TABLE IF NOT EXISTS `notification` (
    `notification_id`     BIGINT      NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id`             BIGINT      NOT NULL COMMENT 'Khóa ngoại -> user',
    `title`               VARCHAR(255),
    `body`                TEXT,
    `type`                VARCHAR(50),
    `related_entity_type` VARCHAR(50),
    `related_entity_id`   BIGINT,
    `is_read`             BOOLEAN     DEFAULT false,
    `sent_at`             TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    `scheduled_for`       TIMESTAMP   NULL,
    PRIMARY KEY (`notification_id`)
);

CREATE TABLE IF NOT EXISTS `academic_warning` (
    `warning_id`   BIGINT      NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id`      BIGINT      NOT NULL COMMENT 'Khóa ngoại -> user',
    `semester_id`  BIGINT      NOT NULL COMMENT 'Khóa ngoại -> semester',
    `warning_type` VARCHAR(100),
    `description`  TEXT,
    `issued_at`    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    `resolved_at`  TIMESTAMP   NULL,
    `status`       VARCHAR(50),
    PRIMARY KEY (`warning_id`)
);

CREATE TABLE IF NOT EXISTS `service_request` (
    `request_id`    BIGINT      NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id`       BIGINT      NOT NULL COMMENT 'Khóa ngoại -> user',
    `service_type`  VARCHAR(100),
    `description`   TEXT,
    `status`        VARCHAR(50) DEFAULT 'chờ xử lý',
    `submitted_at`  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    `resolved_at`   TIMESTAMP   NULL,
    `result_note`   TEXT,
    `attachment_url` TEXT,
    PRIMARY KEY (`request_id`)
);

CREATE TABLE IF NOT EXISTS `feedback` (
    `feedback_id`  BIGINT      NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính',
    `user_id`      BIGINT      NOT NULL COMMENT 'Khóa ngoại -> user',
    `type`         VARCHAR(50),
    `content`      TEXT,
    `submitted_at` TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    `status`       VARCHAR(50) DEFAULT 'chưa đọc',
    `admin_reply`  TEXT,
    PRIMARY KEY (`feedback_id`)
);

-- ─── FOREIGN KEYS ──────────────────────────────────────────────────────────────

ALTER TABLE `user_profile`
    ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
    ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `student_profile`
    ADD FOREIGN KEY (`user_id`)    REFERENCES `user`    (`user_id`)
    ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE `student_profile`
    ADD FOREIGN KEY (`advisor_id`) REFERENCES `advisor` (`advisor_id`)
    ON UPDATE NO ACTION ON DELETE SET NULL;

ALTER TABLE `semester`
    ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `class_section`
    ADD FOREIGN KEY (`course_id`)   REFERENCES `course`   (`course_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE `class_section`
    ADD FOREIGN KEY (`semester_id`) REFERENCES `semester` (`semester_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `enrollment`
    ADD FOREIGN KEY (`user_id`)     REFERENCES `user`     (`user_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE `enrollment`
    ADD FOREIGN KEY (`course_id`)   REFERENCES `course`   (`course_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE `enrollment`
    ADD FOREIGN KEY (`semester_id`) REFERENCES `semester` (`semester_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE `enrollment`
    ADD CONSTRAINT `fk_enrollment_section` FOREIGN KEY (`section_id`) REFERENCES `class_section` (`section_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `schedule`
    ADD FOREIGN KEY (`user_id`)      REFERENCES `user`          (`user_id`)
    ON UPDATE NO ACTION ON DELETE SET NULL;
ALTER TABLE `schedule`
    ADD FOREIGN KEY (`lecturer_id`)  REFERENCES `user`          (`user_id`)
    ON UPDATE NO ACTION ON DELETE SET NULL;
ALTER TABLE `schedule`
    ADD FOREIGN KEY (`section_id`)   REFERENCES `class_section` (`section_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `fee`
    ADD FOREIGN KEY (`user_id`)     REFERENCES `user`     (`user_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE `fee`
    ADD FOREIGN KEY (`semester_id`) REFERENCES `semester` (`semester_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `dormitory_registration`
    ADD FOREIGN KEY (`user_id`)  REFERENCES `user`           (`user_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE `dormitory_registration`
    ADD FOREIGN KEY (`room_id`)  REFERENCES `dormitory_room` (`room_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `curriculum_item`
    ADD FOREIGN KEY (`curriculum_id`) REFERENCES `curriculum` (`curriculum_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE `curriculum_item`
    ADD FOREIGN KEY (`course_id`)     REFERENCES `course`     (`course_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `notification`
    ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `academic_warning`
    ADD FOREIGN KEY (`user_id`)     REFERENCES `user`     (`user_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE `academic_warning`
    ADD FOREIGN KEY (`semester_id`) REFERENCES `semester` (`semester_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `service_request`
    ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE `feedback`
    ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
    ON UPDATE NO ACTION ON DELETE NO ACTION;
-- ============================================================
-- Bảng phân công giáo viên dạy môn theo lớp và kỳ học
-- ============================================================
CREATE TABLE IF NOT EXISTS `teacher_course` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT NOT NULL COMMENT 'FK -> user (STAFF lv2)',
    `course_id`   BIGINT NOT NULL COMMENT 'FK -> course',
    `semester_id` BIGINT NOT NULL COMMENT 'FK -> semester của giáo viên (dùng để xác định kỳ)',
    `class_name`  VARCHAR(50)  NULL COMMENT 'Tên lớp. NULL = dạy tất cả lớp của môn đó',
    `created_at`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_teacher_course` (`user_id`, `course_id`, `semester_id`, `class_name`),
    CONSTRAINT `fk_tc_user`     FOREIGN KEY (`user_id`)     REFERENCES `user`(`user_id`),
    CONSTRAINT `fk_tc_course`   FOREIGN KEY (`course_id`)   REFERENCES `course`(`course_id`),
    CONSTRAINT `fk_tc_semester` FOREIGN KEY (`semester_id`) REFERENCES `semester`(`semester_id`)
);

-- Test data: giáo viên user_id=6 dạy INT101 lớp 65TH3, kỳ semester_id=4
-- (Cần có user STAFF lv2 trong DB để test)
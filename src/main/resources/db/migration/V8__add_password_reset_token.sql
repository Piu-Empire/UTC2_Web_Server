-- V8: Thêm bảng password_reset_token cho chức năng quên mật khẩu
CREATE TABLE IF NOT EXISTS `password_reset_token` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL COMMENT 'Khóa ngoại -> user',
    `token`      VARCHAR(64)  NOT NULL UNIQUE COMMENT 'Token ngẫu nhiên 6 ký tự (OTP)',
    `expires_at` DATETIME     NOT NULL COMMENT 'Thời điểm token hết hạn',
    `used`       BOOLEAN      NOT NULL DEFAULT FALSE COMMENT 'Token đã dùng chưa',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

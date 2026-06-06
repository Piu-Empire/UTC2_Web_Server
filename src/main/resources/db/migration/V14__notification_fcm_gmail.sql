-- ═══════════════════════════════════════════════════════════════
-- V14 — Notification: thêm source + bảng user_notification_setting
-- ═══════════════════════════════════════════════════════════════

-- 1. Thêm cột source vào bảng notification (phân biệt SYSTEM vs GMAIL)
ALTER TABLE `notification`
    ADD COLUMN `source` VARCHAR(20) NOT NULL DEFAULT 'SYSTEM'
        COMMENT 'SYSTEM = thông báo từ server | GMAIL = proxy từ Gmail';

-- 2. Bảng cài đặt thông báo per-user
CREATE TABLE IF NOT EXISTS `user_notification_setting` (
    `user_id`               BIGINT        NOT NULL UNIQUE COMMENT 'FK → user',
    `system_notif_enabled`  BOOLEAN       NOT NULL DEFAULT TRUE  COMMENT 'Bật/tắt thông báo hệ thống',
    `gmail_notif_enabled`   BOOLEAN       NOT NULL DEFAULT FALSE COMMENT 'Bật/tắt thông báo Gmail',
    -- FCM: NULL = chưa cấp quyền hoặc đã từ chối
    `fcm_token`             VARCHAR(512)  NULL     COMMENT 'FCM device token (null = chưa cấp quyền)',
    `fcm_token_updated`     TIMESTAMP     NULL     COMMENT 'Lần cuối cập nhật FCM token',
    -- Gmail: token tạm thời, mã hóa AES-256, không lưu lâu dài
    `gmail_token_enc`       TEXT          NULL     COMMENT 'Google access token (AES-256 encrypted)',
    `gmail_token_expiry`    TIMESTAMP     NULL     COMMENT 'Thời điểm hết hạn Google token',
    `updated_at`            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`)
);

ALTER TABLE `user_notification_setting`
    ADD CONSTRAINT `fk_user_notif_setting_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
    ON UPDATE CASCADE ON DELETE CASCADE;

package com.utc2.appreborn.backend.modules.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Maps to bảng USER_NOTIFICATION_SETTING.
 *
 * Mỗi user có đúng 1 row (được tạo khi user đăng nhập lần đầu
 * hoặc lần đầu gọi GET /notifications/settings).
 *
 * FCM token:
 *   - null  = user chưa cấp quyền hoặc đã từ chối POST_NOTIFICATIONS
 *   - value = token hợp lệ, server sẽ gửi push notification
 *
 * Gmail token:
 *   - Lưu tạm thời, mã hóa AES-256
 *   - Kiểm tra gmail_token_expiry trước khi dùng
 */
@Entity
@Table(name = "user_notification_setting")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationSetting {

    @Id
    @Column(name = "user_id")
    private Long userId;

    /** Bật/tắt thông báo hệ thống (schedule, fee, academic warning, v.v.) */
    @Column(name = "system_notif_enabled", nullable = false)
    @Builder.Default
    private boolean systemNotifEnabled = true;

    /** Bật/tắt thông báo Gmail (user phải link Google account) */
    @Column(name = "gmail_notif_enabled", nullable = false)
    @Builder.Default
    private boolean gmailNotifEnabled = false;

    /** FCM device token — null nếu chưa cấp quyền */
    @Column(name = "fcm_token", length = 512)
    private String fcmToken;

    @Column(name = "fcm_token_updated")
    private LocalDateTime fcmTokenUpdated;

    /** Google access token mã hóa AES-256 — tạm thời, sẽ expire */
    @Column(name = "gmail_token_enc", columnDefinition = "TEXT")
    private String gmailTokenEnc;

    /** Thời điểm Google access token hết hạn */
    @Column(name = "gmail_token_expiry")
    private LocalDateTime gmailTokenExpiry;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Helper methods ────────────────────────────────────────────

    /** Kiểm tra Gmail token còn hạn hay không */
    public boolean isGmailTokenExpired() {
        return gmailTokenExpiry == null
                || LocalDateTime.now().isAfter(gmailTokenExpiry);
    }

    /** Kiểm tra đã liên kết Gmail chưa (có token VÀ chưa hết hạn) */
    public boolean isGmailLinked() {
        return gmailTokenEnc != null && !isGmailTokenExpired();
    }
}

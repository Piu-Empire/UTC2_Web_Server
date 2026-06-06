package com.utc2.appreborn.backend.modules.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** Response trạng thái cài đặt thông báo của user */
@Data
@Builder
public class NotificationSettingResponse {

    private boolean systemNotifEnabled;
    private boolean gmailNotifEnabled;

    /** true = đã link Gmail VÀ token chưa hết hạn */
    private boolean gmailLinked;

    /** Thời điểm Gmail token hết hạn (null nếu chưa link) */
    private LocalDateTime gmailTokenExpiry;

    /** true = đã đăng ký FCM (có token trong DB) */
    private boolean fcmRegistered;
}

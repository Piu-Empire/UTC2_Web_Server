package com.utc2.appreborn.backend.modules.notification.dto;

import lombok.Data;

/** Request cập nhật cài đặt thông báo */
@Data
public class NotificationSettingRequest {

    /** Bật/tắt thông báo hệ thống */
    private Boolean systemNotifEnabled;

    /** Bật/tắt thông báo Gmail */
    private Boolean gmailNotifEnabled;
}

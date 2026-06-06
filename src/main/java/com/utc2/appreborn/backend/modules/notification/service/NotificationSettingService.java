package com.utc2.appreborn.backend.modules.notification.service;

import com.utc2.appreborn.backend.modules.notification.dto.NotificationSettingRequest;
import com.utc2.appreborn.backend.modules.notification.dto.NotificationSettingResponse;

public interface NotificationSettingService {

    /** Lấy cài đặt của user (tạo mới với default nếu chưa có) */
    NotificationSettingResponse getSettings(Long userId);

    /** Cập nhật cài đặt thông báo */
    NotificationSettingResponse updateSettings(Long userId, NotificationSettingRequest request);

    /** Đăng ký hoặc cập nhật FCM token */
    void registerFcmToken(Long userId, String fcmToken);

    /** Xóa FCM token (logout hoặc user từ chối notification) */
    void removeFcmToken(Long userId);
}

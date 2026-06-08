package com.utc2.appreborn.backend.modules.notification.service.impl;

import com.utc2.appreborn.backend.modules.notification.dto.NotificationSettingRequest;
import com.utc2.appreborn.backend.modules.notification.dto.NotificationSettingResponse;
import com.utc2.appreborn.backend.modules.notification.entity.UserNotificationSetting;
import com.utc2.appreborn.backend.modules.notification.repository.UserNotificationSettingRepository;
import com.utc2.appreborn.backend.modules.notification.service.NotificationSettingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationSettingServiceImpl implements NotificationSettingService {

    private final UserNotificationSettingRepository settingRepo;

    /**
     * Lấy cài đặt của user.
     * Nếu chưa có row trong DB → tạo mới với giá trị default.
     */
    @Override
    @Transactional
    public NotificationSettingResponse getSettings(Long userId) {
        UserNotificationSetting setting = getOrCreate(userId);
        return toResponse(setting);
    }

    @Override
    @Transactional
    public NotificationSettingResponse updateSettings(Long userId, NotificationSettingRequest request) {
        UserNotificationSetting setting = getOrCreate(userId);

        // Chỉ cập nhật field không null (partial update)
        if (request.getSystemNotifEnabled() != null) {
            setting.setSystemNotifEnabled(request.getSystemNotifEnabled());
        }
        if (request.getGmailNotifEnabled() != null) {
            setting.setGmailNotifEnabled(request.getGmailNotifEnabled());
        }

        settingRepo.save(setting);
        return toResponse(setting);
    }

    @Override
    @Transactional
    public void registerFcmToken(Long userId, String fcmToken) {
        UserNotificationSetting setting = getOrCreate(userId);
        setting.setFcmToken(fcmToken);
        setting.setFcmTokenUpdated(LocalDateTime.now());
        settingRepo.save(setting);
    }

    @Override
    @Transactional
    public void removeFcmToken(Long userId) {
        settingRepo.findByUserId(userId).ifPresent(setting -> {
            setting.setFcmToken(null);
            setting.setFcmTokenUpdated(null);
            settingRepo.save(setting);
        });
    }

    // ── Helpers ──────────────────────────────────────────────────

    /** Lấy setting, nếu chưa có → tạo với default values */
    private UserNotificationSetting getOrCreate(Long userId) {
        return settingRepo.findByUserId(userId)
                .orElseGet(() -> {
                    UserNotificationSetting def = UserNotificationSetting.builder()
                            .userId(userId)
                            .systemNotifEnabled(true)
                            .gmailNotifEnabled(false)
                            .build();
                    return settingRepo.save(def);
                });
    }

    private NotificationSettingResponse toResponse(UserNotificationSetting s) {
        return NotificationSettingResponse.builder()
                .systemNotifEnabled(s.isSystemNotifEnabled())
                .gmailNotifEnabled(s.isGmailNotifEnabled())
                .gmailLinked(s.isGmailLinked())
                .gmailTokenExpiry(s.getGmailTokenExpiry())
                .fcmRegistered(s.getFcmToken() != null)
                .build();
    }
}

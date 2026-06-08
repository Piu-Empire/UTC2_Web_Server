package com.utc2.appreborn.backend.modules.notification.service;

import com.utc2.appreborn.backend.modules.notification.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    // ── OTP (giữ nguyên, không thay đổi) ──────────────────────────
    void sendOtp(String email);
    void verifyOtp(String email, String otp);

    // ── System Notification CRUD ───────────────────────────────────

    /** Lấy danh sách thông báo của user (phân trang) */
    Page<NotificationResponse> getNotifications(Long userId, Pageable pageable);

    /** Đếm số thông báo chưa đọc */
    long getUnreadCount(Long userId);

    /** Đánh dấu 1 thông báo đã đọc */
    void markAsRead(Long userId, Long notificationId);

    /** Đánh dấu tất cả đã đọc */
    int markAllRead(Long userId);

    /** Xóa 1 thông báo */
    void deleteNotification(Long userId, Long notificationId);

    /**
     * Tạo thông báo hệ thống mới cho user.
     * Dùng bởi các service khác (fee, schedule, academic, v.v.)
     *
     * @param userId            User nhận thông báo
     * @param type              ACADEMIC_WARNING | FEE_DUE | SCHEDULE_CHANGE | ...
     * @param title             Tiêu đề
     * @param body              Nội dung
     * @param relatedEntityType Tên entity liên quan (nullable)
     * @param relatedEntityId   ID entity liên quan (nullable)
     */
    void createSystemNotification(Long userId,
                                  String type,
                                  String title,
                                  String body,
                                  String relatedEntityType,
                                  Long relatedEntityId);
}

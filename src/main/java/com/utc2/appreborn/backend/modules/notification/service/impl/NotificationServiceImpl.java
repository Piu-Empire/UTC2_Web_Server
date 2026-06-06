package com.utc2.appreborn.backend.modules.notification.service.impl;

import com.utc2.appreborn.backend.exception.BadRequestException;
import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.notification.dto.NotificationResponse;
import com.utc2.appreborn.backend.modules.notification.entity.NotificationEntity;
import com.utc2.appreborn.backend.modules.notification.repository.NotificationRepository;
import com.utc2.appreborn.backend.modules.notification.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    // ── OTP Config (giữ nguyên) ──────────────────────────────────
    private static final String ALLOWED_DOMAIN    = "@st.utc2.edu.vn";
    private static final long   OTP_TTL_MS        = 5 * 60 * 1000L;
    private static final int    RATE_LIMIT        = 3;
    private static final long   RATE_WINDOW_MS    = 10 * 60 * 1000L;

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepo;

    private final ConcurrentHashMap<String, OtpEntry>   otpStore   = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateBucket> rateStore  = new ConcurrentHashMap<>();

    // ── OTP (không thay đổi) ─────────────────────────────────────

    @Override
    public void sendOtp(String email) {
        if (!email.endsWith(ALLOWED_DOMAIN)) {
            throw new BadRequestException("Email không hợp lệ");
        }
        checkRateLimit(email);
        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        otpStore.put(email, new OtpEntry(otp, Instant.now().toEpochMilli() + OTP_TTL_MS));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Mã OTP xác thực UTC2");
        message.setText("Mã OTP của bạn là: " + otp + "\nHiệu lực trong 5 phút.");
        mailSender.send(message);
    }

    @Override
    public void verifyOtp(String email, String otp) {
        OtpEntry entry = otpStore.get(email);
        if (entry == null || Instant.now().toEpochMilli() > entry.expiredAt || !entry.otp.equals(otp)) {
            throw new BadRequestException("Mã OTP không đúng hoặc đã hết hạn");
        }
        otpStore.remove(email);
    }

    private void checkRateLimit(String email) {
        long now = Instant.now().toEpochMilli();
        RateBucket bucket = rateStore.compute(email, (k, b) -> {
            if (b == null || now - b.windowStart > RATE_WINDOW_MS) return new RateBucket(now, 1);
            b.count++;
            return b;
        });
        if (bucket.count > RATE_LIMIT) throw new BadRequestException("Vui lòng thử lại sau 10 phút");
    }

    private record OtpEntry(String otp, long expiredAt) {}

    private static class RateBucket {
        long windowStart;
        int  count;
        RateBucket(long windowStart, int count) { this.windowStart = windowStart; this.count = count; }
    }

    // ── System Notification ──────────────────────────────────────

    @Override
    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationRepo
                .findByUserIdOrderBySentAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepo.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        NotificationEntity n = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo không tồn tại"));
        // Chỉ cho phép user sở hữu đánh dấu
        if (!n.getUserId().equals(userId)) {
            throw new BadRequestException("Không có quyền truy cập thông báo này");
        }
        n.setRead(true);
        notificationRepo.save(n);
    }

    @Override
    @Transactional
    public int markAllRead(Long userId) {
        return notificationRepo.markAllReadByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        NotificationEntity n = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo không tồn tại"));
        if (!n.getUserId().equals(userId)) {
            throw new BadRequestException("Không có quyền xóa thông báo này");
        }
        notificationRepo.delete(n);
    }

    @Override
    @Transactional
    public void createSystemNotification(Long userId,
                                         String type,
                                         String title,
                                         String body,
                                         String relatedEntityType,
                                         Long relatedEntityId) {
        NotificationEntity n = NotificationEntity.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .body(body)
                .source("SYSTEM")
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .build();
        notificationRepo.save(n);
    }

    // ── Mapper ───────────────────────────────────────────────────

    private NotificationResponse toResponse(NotificationEntity entity) {
        return NotificationResponse.builder()
                .notificationId(entity.getNotificationId())
                .title(entity.getTitle())
                .body(entity.getBody())
                .type(entity.getType())
                .source(entity.getSource())
                .isRead(entity.isRead())
                .sentAt(entity.getSentAt())
                .relatedEntityType(entity.getRelatedEntityType())
                .relatedEntityId(entity.getRelatedEntityId())
                .build();
    }
}

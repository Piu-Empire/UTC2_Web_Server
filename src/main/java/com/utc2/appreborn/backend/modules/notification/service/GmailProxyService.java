package com.utc2.appreborn.backend.modules.notification.service;

import com.utc2.appreborn.backend.modules.notification.dto.GmailMessageResponse;
import com.utc2.appreborn.backend.modules.notification.dto.GmailStatusResponse;

import java.util.List;

public interface GmailProxyService {

    /**
     * Lưu Google access token tạm thời (mã hóa AES-256) cho user.
     * Token có hiệu lực ~1h, sau đó user cần cấp lại.
     *
     * @param userId            User liên kết
     * @param googleAccessToken Token nhận từ Google Sign-In trên App
     */
    void linkGmail(Long userId, String googleAccessToken);

    /** Hủy liên kết Gmail — xóa token khỏi DB */
    void unlinkGmail(Long userId);

    /** Kiểm tra trạng thái liên kết Gmail của user */
    GmailStatusResponse getStatus(Long userId);

    /**
     * Lấy danh sách email quan trọng từ Gmail (tối đa 20 email gần nhất).
     * Server dùng stored token để gọi Gmail API.
     *
     * @throws com.utc2.appreborn.backend.exception.BadRequestException nếu token hết hạn / chưa link
     */
    List<GmailMessageResponse> getInbox(Long userId);
}

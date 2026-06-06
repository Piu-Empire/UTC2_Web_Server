package com.utc2.appreborn.backend.modules.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** Trạng thái liên kết Gmail của user */
@Data
@Builder
public class GmailStatusResponse {

    /** true = đã link VÀ token chưa hết hạn */
    private boolean linked;

    /** true = đã link nhưng token đã hết hạn (cần kết nối lại) */
    private boolean expired;

    /** Thời điểm token hết hạn (null nếu chưa link) */
    private LocalDateTime expiry;
}

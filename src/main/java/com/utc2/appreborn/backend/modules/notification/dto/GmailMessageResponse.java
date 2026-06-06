package com.utc2.appreborn.backend.modules.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** Đại diện 1 email từ Gmail (qua server proxy) */
@Data
@Builder
public class GmailMessageResponse {

    /** Gmail message ID */
    private String messageId;

    private String subject;
    private String from;

    /** Đoạn preview ngắn (snippet từ Gmail API) */
    private String snippet;

    private LocalDateTime receivedAt;
    private boolean isUnread;
}

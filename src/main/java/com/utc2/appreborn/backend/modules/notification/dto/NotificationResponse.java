package com.utc2.appreborn.backend.modules.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** Response cho 1 thông báo hệ thống (source = SYSTEM) */
@Data
@Builder
public class NotificationResponse {

    private Long notificationId;
    private String title;
    private String body;

    /** Loại: ACADEMIC_WARNING | FEE_DUE | SCHEDULE_CHANGE | ENROLLMENT_UPDATE | DORMITORY_STATUS | GENERAL */
    private String type;

    /** Nguồn: SYSTEM | GMAIL */
    private String source;

    @JsonProperty("isRead")
    private boolean isRead;

    private LocalDateTime sentAt;
    private String relatedEntityType;
    private Long relatedEntityId;
}

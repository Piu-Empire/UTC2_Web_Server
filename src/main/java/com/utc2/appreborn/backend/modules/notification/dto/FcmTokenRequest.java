package com.utc2.appreborn.backend.modules.notification.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/** Request đăng ký / cập nhật FCM device token */
@Data
public class FcmTokenRequest {

    @NotBlank(message = "FCM token không được để trống")
    private String fcmToken;
}

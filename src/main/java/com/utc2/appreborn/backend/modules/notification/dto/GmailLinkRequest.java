package com.utc2.appreborn.backend.modules.notification.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/** Request liên kết Gmail — App gửi Google access token tạm lên server */
@Data
public class GmailLinkRequest {

    @NotBlank(message = "Google access token không được để trống")
    private String googleAccessToken;
}

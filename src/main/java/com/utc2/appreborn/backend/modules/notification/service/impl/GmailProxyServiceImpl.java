package com.utc2.appreborn.backend.modules.notification.service.impl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.utc2.appreborn.backend.exception.BadRequestException;
import com.utc2.appreborn.backend.modules.notification.dto.GmailMessageResponse;
import com.utc2.appreborn.backend.modules.notification.dto.GmailStatusResponse;
import com.utc2.appreborn.backend.modules.notification.entity.UserNotificationSetting;
import com.utc2.appreborn.backend.modules.notification.repository.UserNotificationSettingRepository;
import com.utc2.appreborn.backend.modules.notification.service.GmailProxyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailProxyServiceImpl implements GmailProxyService {

    private final UserNotificationSettingRepository settingRepo;

    /** Secret key 16 bytes (128-bit) AES — cấu hình trong application.properties */
    @Value("${app.gmail.aes-secret:UTC2AppSecretKey!}")
    private String aesSecret;

    // Gmail token có hiệu lực ~1 giờ
    private static final long GMAIL_TOKEN_VALIDITY_MINUTES = 55;

    // ── Link / Unlink ────────────────────────────────────────────

    @Override
    @Transactional
    public void linkGmail(Long userId, String googleAccessToken) {
        UserNotificationSetting setting = getOrCreate(userId);

        // Mã hóa token trước khi lưu
        String encrypted = encryptAes(googleAccessToken);
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(GMAIL_TOKEN_VALIDITY_MINUTES);

        setting.setGmailTokenEnc(encrypted);
        setting.setGmailTokenExpiry(expiry);
        setting.setGmailNotifEnabled(true);
        settingRepo.save(setting);

        log.info("Gmail linked for user {}, expires at {}", userId, expiry);
    }

    @Override
    @Transactional
    public void unlinkGmail(Long userId) {
        settingRepo.findByUserId(userId).ifPresent(setting -> {
            setting.setGmailTokenEnc(null);
            setting.setGmailTokenExpiry(null);
            setting.setGmailNotifEnabled(false);
            settingRepo.save(setting);
        });
        log.info("Gmail unlinked for user {}", userId);
    }

    // ── Status ───────────────────────────────────────────────────

    @Override
    public GmailStatusResponse getStatus(Long userId) {
        UserNotificationSetting setting = settingRepo.findByUserId(userId).orElse(null);
        if (setting == null || setting.getGmailTokenEnc() == null) {
            return GmailStatusResponse.builder().linked(false).expired(false).build();
        }
        boolean expired = setting.isGmailTokenExpired();
        return GmailStatusResponse.builder()
                .linked(!expired)
                .expired(expired)
                .expiry(setting.getGmailTokenExpiry())
                .build();
    }

    // ── Inbox ────────────────────────────────────────────────────

    @Override
    public List<GmailMessageResponse> getInbox(Long userId) {
        UserNotificationSetting setting = settingRepo.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Chưa liên kết Gmail"));

        if (setting.getGmailTokenEnc() == null) {
            throw new BadRequestException("Chưa liên kết Gmail");
        }
        if (setting.isGmailTokenExpired()) {
            throw new BadRequestException("Token Gmail đã hết hạn. Vui lòng kết nối lại.");
        }

        // Giải mã token
        String accessToken = decryptAes(setting.getGmailTokenEnc());

        try {
            // Tạo Gmail API client với access token tạm thời
            GoogleCredentials credentials = GoogleCredentials.create(
                    new AccessToken(accessToken,
                            Date.from(setting.getGmailTokenExpiry()
                                    .atZone(ZoneId.systemDefault()).toInstant()))
            );

            Gmail gmailService = new Gmail.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("UTC2-AppReborn")
                    .build();

            // Lấy tối đa 20 email gần nhất từ INBOX thỏa mãn bộ lọc
            String query = "from:(*@classroom.google.com OR fitutclms@gmail.com OR *@st.utc2.edu.vn)";
            ListMessagesResponse listResponse = gmailService.users().messages()
                    .list("me")
                    .setLabelIds(List.of("INBOX"))
                    .setQ(query)
                    .setMaxResults(20L)
                    .execute();

            List<GmailMessageResponse> result = new ArrayList<>();
            if (listResponse.getMessages() == null) return result;

            for (Message msgRef : listResponse.getMessages()) {
                try {
                    Message msg = gmailService.users().messages()
                            .get("me", msgRef.getId())
                            .setFormat("metadata")
                            .setMetadataHeaders(List.of("Subject", "From", "Date"))
                            .execute();

                    result.add(parseMessage(msg));
                } catch (Exception e) {
                    log.warn("Lỗi đọc message {}: {}", msgRef.getId(), e.getMessage());
                }
            }
            return result;

        } catch (Exception e) {
            log.error("Gmail API error for user {}: {}", userId, e.getMessage());
            throw new BadRequestException("Không thể đọc Gmail: " + e.getMessage());
        }
    }

    // ── Private Helpers ──────────────────────────────────────────

    private GmailMessageResponse parseMessage(Message msg) {
        String subject = "", from = "", date = "";
        boolean isUnread = msg.getLabelIds() != null && msg.getLabelIds().contains("UNREAD");

        if (msg.getPayload() != null && msg.getPayload().getHeaders() != null) {
            for (MessagePartHeader h : msg.getPayload().getHeaders()) {
                switch (h.getName()) {
                    case "Subject" -> subject = h.getValue();
                    case "From"    -> from    = h.getValue();
                    case "Date"    -> date    = h.getValue();
                }
            }
        }

        LocalDateTime receivedAt = null;
        if (msg.getInternalDate() != null) {
            receivedAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(msg.getInternalDate()),
                    ZoneId.systemDefault());
        }

        return GmailMessageResponse.builder()
                .messageId(msg.getId())
                .subject(subject)
                .from(from)
                .snippet(msg.getSnippet())
                .receivedAt(receivedAt)
                .isUnread(isUnread)
                .build();
    }

    private UserNotificationSetting getOrCreate(Long userId) {
        return settingRepo.findByUserId(userId)
                .orElseGet(() -> settingRepo.save(
                        UserNotificationSetting.builder().userId(userId).build()));
    }

    // ── AES Encryption (token bảo mật) ───────────────────────────

    private String encryptAes(String plainText) {
        try {
            SecretKeySpec key = buildKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(
                    plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa token Gmail", e);
        }
    }

    private String decryptAes(String cipherText) {
        try {
            SecretKeySpec key = buildKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)),
                    StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi giải mã token Gmail", e);
        }
    }

    /** Đảm bảo key đúng 16 bytes (AES-128) */
    private SecretKeySpec buildKey() {
        byte[] raw = aesSecret.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = new byte[16];
        System.arraycopy(raw, 0, keyBytes, 0, Math.min(raw.length, 16));
        return new SecretKeySpec(keyBytes, "AES");
    }
}

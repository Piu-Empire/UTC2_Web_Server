package com.utc2.appreborn.backend.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    /**
     * Gửi email OTP đặt lại mật khẩu.
     *
     * @param toEmail địa chỉ email nhận
     * @param otp     mã OTP 6 chữ số
     */
    public void sendPasswordResetEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[UTC2 App] Đặt lại mật khẩu");
        message.setText(
                "Xin chào,\n\n" +
                "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản UTC2 App.\n\n" +
                "Mã OTP của bạn là: " + otp + "\n\n" +
                "Mã này có hiệu lực trong 15 phút.\n" +
                "Nếu bạn không yêu cầu, hãy bỏ qua email này.\n\n" +
                "Trân trọng,\nĐội ngũ UTC2 App"
        );
        mailSender.send(message);
    }
}

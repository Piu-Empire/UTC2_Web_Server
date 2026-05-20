package com.utc2.appreborn.backend.modules.notification.service;

public interface NotificationService {
    void sendOtp(String email);
    void verifyOtp(String email, String otp);
}

package com.utc2.appreborn.backend.security.password;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @SuppressWarnings("deprecation")
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Tắt mã hóa BCrypt, chấp nhận mật khẩu dạng chữ thô để tiện test
        return NoOpPasswordEncoder.getInstance();
    }
}
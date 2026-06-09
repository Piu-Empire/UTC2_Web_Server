package com.utc2.appreborn.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = null;
                
                // 1. Thử đọc từ đường dẫn tuyệt đối trên Render (Secret Files)
                File renderSecretFile = new File("/etc/secrets/firebase-service-account.json");
                if (renderSecretFile.exists()) {
                    serviceAccount = new FileInputStream(renderSecretFile);
                    log.info("Đang đọc Firebase config từ /etc/secrets/...");
                } else {
                    // 2. Đọc từ resources (khi chạy local)
                    serviceAccount = getClass().getClassLoader()
                            .getResourceAsStream("firebase-service-account.json");
                    log.info("Đang đọc Firebase config từ resources...");
                }
                
                if (serviceAccount != null) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase application has been initialized");
                } else {
                    log.warn("Lỗi: Không tìm thấy file firebase-service-account.json ở cả Render Secrets lẫn resources. Push Notification sẽ không hoạt động!");
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo FirebaseApp. Push notification có thể không hoạt động.", e);
        }
    }
}

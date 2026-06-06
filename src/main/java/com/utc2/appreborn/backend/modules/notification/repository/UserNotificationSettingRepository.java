package com.utc2.appreborn.backend.modules.notification.repository;

import com.utc2.appreborn.backend.modules.notification.entity.UserNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, Long> {

    Optional<UserNotificationSetting> findByUserId(Long userId);
}

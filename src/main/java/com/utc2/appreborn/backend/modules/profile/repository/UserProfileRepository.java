package com.utc2.appreborn.backend.modules.profile.repository;

import com.utc2.appreborn.backend.modules.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

// FIX: UserProfile dùng @MapsId nên userId chính là @Id.
// Spring Data không cho phép derive findByUserId() khi userId là @Id —
// trùng với findById() sẵn có từ JpaRepository.
// Xóa findByUserId() và dùng findById(userId) ở tất cả nơi gọi.
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    // findById(Long id) đã có sẵn từ JpaRepository — dùng trực tiếp
}
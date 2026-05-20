package com.utc2.appreborn.backend.modules.profile.repository;

import com.utc2.appreborn.backend.modules.profile.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


// FIX: UserProfile dùng @MapsId nên userId chính là @Id.
// Spring Data không cho phép derive findByUserId() khi userId là @Id —
// trùng với findById() sẵn có từ JpaRepository.
// Xóa findByUserId() và dùng findById(userId) ở tất cả nơi gọi.
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {
    // findById(Long id) đã có sẵn từ JpaRepository — dùng trực tiếp
}
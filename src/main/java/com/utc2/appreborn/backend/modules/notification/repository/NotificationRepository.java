package com.utc2.appreborn.backend.modules.notification.repository;

import com.utc2.appreborn.backend.modules.notification.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    /** Lấy danh sách thông báo của user, mới nhất trước */
    Page<NotificationEntity> findByUserIdOrderBySentAtDesc(Long userId, Pageable pageable);

    /** Đếm số thông báo chưa đọc */
    long countByUserIdAndIsReadFalse(Long userId);

    /** Đánh dấu tất cả thông báo của user là đã đọc */
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    int markAllReadByUserId(@Param("userId") Long userId);
}

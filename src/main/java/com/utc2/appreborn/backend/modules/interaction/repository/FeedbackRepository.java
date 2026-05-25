package com.utc2.appreborn.backend.modules.interaction.repository;

import com.utc2.appreborn.backend.modules.interaction.entity.FeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {

    /** Sinh viên: lịch sử của mình */
    List<FeedbackEntity> findByUserIdOrderBySubmittedAtDesc(Long userId);

    /** Admin: lọc theo status */
    List<FeedbackEntity> findByStatusOrderBySubmittedAtDesc(String status);

    /** Admin: tất cả */
    List<FeedbackEntity> findAllByOrderBySubmittedAtDesc();
}
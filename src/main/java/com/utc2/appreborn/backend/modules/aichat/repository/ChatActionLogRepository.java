package com.utc2.appreborn.backend.modules.aichat.repository;

import com.utc2.appreborn.backend.modules.aichat.entity.ChatActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatActionLogRepository extends JpaRepository<ChatActionLog, Long> {
}

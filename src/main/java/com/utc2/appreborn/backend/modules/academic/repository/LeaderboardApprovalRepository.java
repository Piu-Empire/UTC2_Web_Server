package com.utc2.appreborn.backend.modules.academic.repository;

import com.utc2.appreborn.backend.modules.academic.entity.LeaderboardApprovalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LeaderboardApprovalRepository extends JpaRepository<LeaderboardApprovalEntity, Long> {
    boolean existsBySemesterId(Long semesterId);
    Optional<LeaderboardApprovalEntity> findBySemesterId(Long semesterId);
}
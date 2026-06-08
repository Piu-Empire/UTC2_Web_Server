package com.utc2.appreborn.backend.modules.academic.repository;

import com.utc2.appreborn.backend.modules.academic.entity.AcademicWarningEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AcademicWarningRepository extends JpaRepository<AcademicWarningEntity, Long> {

    /** App: chỉ lấy warning đã duyệt (approved_at IS NOT NULL) */
    @Query("SELECT w FROM AcademicWarningEntity w WHERE w.userId = :uid AND w.approvedAt IS NOT NULL ORDER BY w.issuedAt DESC")
    List<AcademicWarningEntity> findApprovedByUserId(@Param("uid") Long userId);

    @Query("SELECT w FROM AcademicWarningEntity w WHERE w.userId = :uid AND w.semesterId = :sid AND w.approvedAt IS NOT NULL ORDER BY w.issuedAt DESC")
    List<AcademicWarningEntity> findApprovedByUserIdAndSemesterId(@Param("uid") Long userId, @Param("sid") Long semesterId);

    /** Admin: lấy tất cả kể cả pending */
    List<AcademicWarningEntity> findByUserIdOrderByIssuedAtDesc(Long userId);
    List<AcademicWarningEntity> findByUserIdAndSemesterIdOrderByIssuedAtDesc(Long userId, Long semesterId);

    /** Pending list cho lv5 duyệt */
    List<AcademicWarningEntity> findByApprovedAtIsNullOrderByIssuedAtDesc();
}
package com.utc2.appreborn.backend.modules.academic.repository;

import com.utc2.appreborn.backend.modules.academic.entity.AcademicWarningEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademicWarningRepository extends JpaRepository<AcademicWarningEntity, Long> {

    List<AcademicWarningEntity> findByUserIdOrderByIssuedAtDesc(Long userId);

    List<AcademicWarningEntity> findByUserIdAndSemesterIdOrderByIssuedAtDesc(Long userId, Long semesterId);
}

package com.utc2.appreborn.backend.modules.academic.repository;

import com.utc2.appreborn.backend.modules.academic.entity.AcademicWarningEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicWarningRepository extends JpaRepository<AcademicWarningEntity, Long> {

    List<AcademicWarningEntity> findByUserIdOrderByIssuedAtDesc(Long userId);

    List<AcademicWarningEntity> findByUserIdAndSemesterIdOrderByIssuedAtDesc(Long userId, Long semesterId);
}
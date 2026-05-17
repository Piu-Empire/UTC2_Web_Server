package com.utc2.appreborn.backend.modules.academic.repository;

import com.utc2.appreborn.backend.modules.academic.entity.SemesterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SemesterRepository extends JpaRepository<SemesterEntity, Long> {

    List<SemesterEntity> findByUserIdOrderBySemesterIdDesc(Long userId);
}
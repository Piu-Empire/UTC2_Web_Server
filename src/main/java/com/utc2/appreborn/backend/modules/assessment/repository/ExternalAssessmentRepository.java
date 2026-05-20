package com.utc2.appreborn.backend.modules.assessment.repository;

import com.utc2.appreborn.backend.modules.assessment.entity.ExternalAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExternalAssessmentRepository extends JpaRepository<ExternalAssessment, Long> {

    List<ExternalAssessment> findByUserIdAndPeriodId(Long userId, String periodId);

    void deleteByUserIdAndPeriodId(Long userId, String periodId);
}
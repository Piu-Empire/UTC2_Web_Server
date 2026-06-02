package com.utc2.appreborn.backend.modules.assessment.repository;

import com.utc2.appreborn.backend.modules.assessment.entity.ExternalAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExternalAssessmentRepository extends JpaRepository<ExternalAssessment, Long> {

    List<ExternalAssessment> findByUserIdAndPeriodId(Long userId, String periodId);

    Optional<ExternalAssessment> findByUserIdAndPeriodIdAndCriteriaId(Long userId, String periodId, Integer criteriaId);

    List<ExternalAssessment> findByPeriodId(String periodId);

    void deleteByUserIdAndPeriodId(Long userId, String periodId);
}
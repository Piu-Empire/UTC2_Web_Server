package com.utc2.appreborn.backend.modules.assessment.repository;

import com.utc2.appreborn.backend.modules.assessment.entity.AdvisorAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdvisorAssessmentRepository extends JpaRepository<AdvisorAssessment, Long> {

    List<AdvisorAssessment> findByUserIdAndPeriodId(Long userId, String periodId);

    List<AdvisorAssessment> findByPeriodId(String periodId);

    void deleteByUserIdAndPeriodId(Long userId, String periodId);
}
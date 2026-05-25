package com.utc2.appreborn.backend.modules.assessment.repository;

import com.utc2.appreborn.backend.modules.assessment.entity.AdvisorAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdvisorAssessmentRepository extends JpaRepository<AdvisorAssessment, Long> {

    List<AdvisorAssessment> findByUserIdAndPeriodId(Long userId, String periodId);

    List<AdvisorAssessment> findByPeriodId(String periodId);

    @Modifying
    @Query("DELETE FROM AdvisorAssessment aa WHERE aa.userId = :userId AND aa.periodId = :periodId")
    void deleteByUserIdAndPeriodId(@Param("userId") Long userId, @Param("periodId") String periodId);
}
package com.utc2.appreborn.backend.modules.assessment.repository;

import com.utc2.appreborn.backend.modules.assessment.entity.AssessmentPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentPeriodRepository extends JpaRepository<AssessmentPeriod, String> {
    List<AssessmentPeriod> findAllByOrderByPeriodIdDesc();
}
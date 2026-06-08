package com.utc2.appreborn.backend.modules.assessment.repository;

import com.utc2.appreborn.backend.modules.assessment.entity.ExternalAssessmentStatus;
import com.utc2.appreborn.backend.modules.assessment.entity.ExternalAssessmentStatusId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExternalAssessmentStatusRepository
        extends JpaRepository<ExternalAssessmentStatus, ExternalAssessmentStatusId> {

    List<ExternalAssessmentStatus> findByPeriodId(String periodId);
}
package com.utc2.appreborn.backend.modules.assessment.repository;

import com.utc2.appreborn.backend.modules.assessment.entity.StudentAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentAssessmentRepository extends JpaRepository<StudentAssessment, Long> {

    List<StudentAssessment> findByUserIdAndPeriodId(Long userId, String periodId);

    // Admin: lấy tất cả sinh viên theo học kỳ
    List<StudentAssessment> findByPeriodId(String periodId);

    // Xóa hết để upsert batch
    void deleteByUserIdAndPeriodId(Long userId, String periodId);
}
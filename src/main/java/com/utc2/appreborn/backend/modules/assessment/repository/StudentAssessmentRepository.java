package com.utc2.appreborn.backend.modules.assessment.repository;

import com.utc2.appreborn.backend.modules.assessment.entity.StudentAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentAssessmentRepository extends JpaRepository<StudentAssessment, Long> {

    List<StudentAssessment> findByUserIdAndPeriodId(Long userId, String periodId);

    // Admin: lấy tất cả sinh viên theo học kỳ
    List<StudentAssessment> findByPeriodId(String periodId);

    // Xóa hết để upsert batch — dùng @Modifying để flush DELETE ngay trước INSERT
    @Modifying
    @Query("DELETE FROM StudentAssessment sa WHERE sa.userId = :userId AND sa.periodId = :periodId")
    void deleteByUserIdAndPeriodId(@Param("userId") Long userId, @Param("periodId") String periodId);
}
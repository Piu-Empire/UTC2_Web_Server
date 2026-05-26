package com.utc2.appreborn.backend.modules.academic.service;

import com.utc2.appreborn.backend.modules.academic.dto.*;

import java.util.List;

public interface AcademicService {

    List<SemesterDto>         getSemesters(Long userId);

    List<CourseGradeDto>      getGrades(Long userId, Long semesterId);

    List<LeaderboardEntryDto> getLeaderboard(Long semesterId, String academicYear);

    List<ScholarshipDto>      getScholarships(Long userId);

    List<AcademicWarningDto>  getWarnings(Long userId, Long semesterId);
}

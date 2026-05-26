package com.utc2.appreborn.backend.modules.academic.service;

import com.utc2.appreborn.backend.modules.academic.dto.*;

import java.util.List;

public interface AcademicService {

    List<SemesterDto> getSemesters();

    List<CourseGradeDto> getGrades(Long semesterId);

    List<LeaderboardEntryDto> getLeaderboard(Long semesterId, String academicYear);

    List<ScholarshipDto> getScholarships();

    List<AcademicWarningDto> getWarnings(Long semesterId);
}

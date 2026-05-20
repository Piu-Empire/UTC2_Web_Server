package com.utc2.appreborn.backend.modules.academic.service;

import com.utc2.appreborn.backend.modules.academic.dto.*;

import java.util.List;

public interface AcademicService {

    /** Danh sách kỳ học của sinh viên đang đăng nhập */
    List<SemesterDto> getSemesters();

    /**
     * Bảng điểm của sinh viên đang đăng nhập.
     *
     * @param semesterId null → trả tất cả kỳ; có giá trị → lọc theo kỳ đó.
     */
    List<CourseGradeDto> getGrades(Long semesterId);

    /**
     * Bảng xếp hạng.
     *
     * @param semesterId   Xếp hạng theo kỳ học cụ thể (ưu tiên nếu có).
     * @param academicYear Xếp hạng theo năm học (dùng khi semesterId == null).
     */
    List<LeaderboardEntryDto> getLeaderboard(Long semesterId, String academicYear);

    /** Danh sách học bổng kèm trạng thái của sinh viên đang đăng nhập */
    List<ScholarshipDto> getScholarships();

    /**
     * Cảnh báo học vụ của sinh viên đang đăng nhập.
     *
     * @param semesterId null → trả tất cả; có giá trị → lọc theo kỳ.
     */
    List<AcademicWarningDto> getWarnings(Long semesterId);
}
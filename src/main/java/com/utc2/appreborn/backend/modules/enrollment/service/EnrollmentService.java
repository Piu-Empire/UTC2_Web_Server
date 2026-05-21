package com.utc2.appreborn.backend.modules.enrollment.service;

import com.utc2.appreborn.backend.modules.enrollment.dto.CourseItemDto;
import com.utc2.appreborn.backend.modules.enrollment.dto.EnrollRequest;
import com.utc2.appreborn.backend.modules.enrollment.dto.EnrollmentItemDto;

import java.util.List;

public interface EnrollmentService {

    /** Lấy toàn bộ môn đã đăng ký + điểm của sinh viên đang đăng nhập */
    List<EnrollmentItemDto> getMyEnrollments();

    /** Danh sách tất cả môn học, đánh dấu môn đã đăng ký */
    List<CourseItemDto> getAvailableCourses();

    /** Đăng ký 1 môn học */
    EnrollmentItemDto enroll(EnrollRequest request);

    /** Hủy đăng ký 1 môn học */
    void cancelEnrollment(Long enrollmentId);
}

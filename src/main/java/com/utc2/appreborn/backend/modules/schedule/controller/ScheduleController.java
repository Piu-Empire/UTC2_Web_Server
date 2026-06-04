package com.utc2.appreborn.backend.modules.schedule.controller;

import com.utc2.appreborn.backend.common.response.ApiResponse;
import com.utc2.appreborn.backend.modules.schedule.dto.*;
import com.utc2.appreborn.backend.modules.schedule.service.ScheduleService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // ════════════════════════════════════════════════════════════════════════
    // MOBILE — public, không cần token (đã permitAll trong SecurityConfig)
    // Base: /api/v1/schedule
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/schedule/meta?studentCode=2211020001
     * App gọi khi mở — so sánh lastUpdated với file local để quyết định download.
     */
    @GetMapping("/api/v1/schedule/meta")
    public ResponseEntity<ApiResponse<ScheduleMetaDto>> getMeta(
            @RequestParam String studentCode) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getMeta(studentCode)));
    }

    /**
     * GET /api/v1/schedule/file?studentCode=2211020001
     * Trả về toàn bộ lịch dạng JSON. App lưu thành file local.
     */
    @GetMapping("/api/v1/schedule/file")
    public ResponseEntity<ApiResponse<ScheduleFileDto>> getFile(
            @RequestParam String studentCode) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getScheduleFile(studentCode)));
    }

    // ════════════════════════════════════════════════════════════════════════
    // ADMIN WEB — yêu cầu token (anyRequest().authenticated())
    // Base: /api/v1/admin/schedules
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/admin/schedules
     * Toàn bộ lịch, có filter + phân trang.
     */
    /**
     * GET /api/v1/admin/schedules
     * Toàn bộ lịch, có filter + phân trang theo từng thuộc tính.
     */
    @GetMapping("/api/v1/admin/schedules")
    public ResponseEntity<ApiResponse<Page<ScheduleResponse>>> getAll(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Integer scheduleType,
            @RequestParam(required = false) Long lecturerId,
            @RequestParam(required = false) String sectionCode,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) Integer dayOfWeek,
            @RequestParam(required = false) Integer period,
            @RequestParam(required = false) String room,
            @RequestParam(required = false) String lecturerName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                scheduleService.getAll(
                        semesterId, scheduleType, lecturerId,
                        sectionCode, courseName, dayOfWeek, period, room, lecturerName,
                        pageable)));
    }

    /**
     * POST /api/v1/admin/schedules/upsert
     * Thêm hoặc cập nhật 1 lịch (tự phát hiện trùng theo sectionId + scheduleType +
     * dayOfWeek + startPeriod).
     */
    @PostMapping("/api/v1/admin/schedules/upsert")
    public ResponseEntity<ApiResponse<ScheduleResponse>> upsert(
            @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.upsert(request)));
    }

    /**
     * POST /api/v1/admin/schedules/bulk
     * Thêm hoặc cập nhật danh sách lịch (upsert từng phần tử).
     */
    @PostMapping("/api/v1/admin/schedules/bulk")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> bulkUpsert(
            @RequestBody List<ScheduleRequest> requests) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.bulkUpsert(requests)));
    }

    /**
     * GET /api/v1/admin/schedules/by-section
     * Tra TKB theo lớp học phần.
     *
     * @param sectionId    (optional) filter đúng section_id
     * @param semesterId   (optional) filter học kỳ
     * @param scheduleType (optional) 1/2/3
     * @param keyword      (optional) tìm section_code hoặc room
     * @param page         số trang (default 0)
     * @param size         kích thước trang (default 10)
     */
    @GetMapping("/api/v1/admin/schedules/by-section")
    public ResponseEntity<ApiResponse<Page<ScheduleResponse>>> getBySection(
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Integer scheduleType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                scheduleService.getBySection(sectionId, semesterId, scheduleType, keyword, pageable)));
    }

    /**
     * GET /api/v1/admin/schedules/by-lecturer
     * Tra TKB theo giảng viên.
     *
     * @param lecturerId   (optional) filter chính xác theo lecturer_id
     * @param lecturerName (optional) tìm gần đúng theo tên
     * @param semesterId   (optional) filter học kỳ
     * @param scheduleType (optional) 1/2/3
     */
    @GetMapping("/api/v1/admin/schedules/by-lecturer")
    public ResponseEntity<ApiResponse<Page<ScheduleResponse>>> getByLecturer(
            @RequestParam(required = false) Long lecturerId,
            @RequestParam(required = false) String lecturerName,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Integer scheduleType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                scheduleService.getByLecturer(lecturerId, lecturerName, semesterId, scheduleType, pageable)));
    }

    /**
     * GET /api/v1/admin/schedules/{id}
     * Chi tiết một bản ghi lịch.
     */
    @GetMapping("/api/v1/admin/schedules/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getById(id)));
    }

    /**
     * POST /api/v1/admin/schedules
     * Tạo mới lịch thủ công.
     */
    @PostMapping("/api/v1/admin/schedules")
    public ResponseEntity<ApiResponse<ScheduleResponse>> create(
            @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.create(request)));
    }

    /**
     * PUT /api/v1/admin/schedules/{id}
     * Cập nhật lịch.
     */
    @PutMapping("/api/v1/admin/schedules/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> update(
            @PathVariable Long id,
            @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.update(id, request)));
    }

    /**
     * DELETE /api/v1/admin/schedules/{id}
     */
    @DeleteMapping("/api/v1/admin/schedules/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * POST /api/v1/admin/schedules/import
     * Import Excel. Form-data: file, scheduleType (1/2/3), overwrite (true/false).
     */
    @PostMapping("/api/v1/admin/schedules/import")
    public ResponseEntity<ApiResponse<ImportResultDto>> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("scheduleType") Integer scheduleType,
            @RequestParam(value = "overwrite", defaultValue = "false") boolean overwrite) throws IOException {

        ImportResultDto result = scheduleService.importExcel(file, scheduleType, overwrite);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/v1/admin/schedules/export
     * Export Excel — trả về file .xlsx trực tiếp.
     *
     * @param semesterId   (optional)
     * @param scheduleType (optional) 1/2/3
     * @param lecturerId   (optional)
     * @param room         (optional) tìm gần đúng
     */
    @GetMapping("/api/v1/admin/schedules/export")
    public void exportExcel(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Integer scheduleType,
            @RequestParam(required = false) String lecturerName,
            @RequestParam(required = false) String room,
            @RequestParam(required = false) String sectionCode,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) Integer dayOfWeek,
            @RequestParam(required = false) Integer weekStart,
            @RequestParam(required = false) Integer weekEnd,
            HttpServletResponse response) throws IOException {

        scheduleService.exportExcel(semesterId, scheduleType, lecturerName, room,
                sectionCode, courseName, dayOfWeek, weekStart, weekEnd, response);
    }

    // ════════════════════════════════════════════════════════════════════════
    // SUGGEST — autocomplete distinct values từ bảng schedule
    // ════════════════════════════════════════════════════════════════════════
    /**
     * GET /api/v1/admin/schedules/suggest/sections?keyword=MATH
     * Distinct section_code từ bảng schedule (đã có lịch).
     */
    @GetMapping("/api/v1/admin/schedules/suggest/sections")
    public ResponseEntity<ApiResponse<List<String>>> suggestSections(
            @RequestParam(required = false) String keyword) {

        String sql = keyword != null && !keyword.isBlank()
                ? "SELECT DISTINCT cs.section_code FROM schedule s JOIN class_section cs ON cs.section_id = s.section_id WHERE cs.section_code LIKE :kw ORDER BY cs.section_code LIMIT 20"
                : "SELECT DISTINCT cs.section_code FROM schedule s JOIN class_section cs ON cs.section_id = s.section_id ORDER BY cs.section_code LIMIT 20";

        var query = em.createNativeQuery(sql);
        if (keyword != null && !keyword.isBlank())
            query.setParameter("kw", "%" + keyword.trim() + "%");

        @SuppressWarnings("unchecked")
        List<String> result = query.getResultList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/v1/admin/schedules/suggest/courses?keyword=Toan
     * Distinct course_name từ các môn đang có lịch.
     */
    @GetMapping("/api/v1/admin/schedules/suggest/courses")
    public ResponseEntity<ApiResponse<List<String>>> suggestCourses(
            @RequestParam(required = false) String keyword) {

        String sql = keyword != null && !keyword.isBlank()
                ? "SELECT DISTINCT c.course_name FROM schedule s JOIN class_section cs ON cs.section_id = s.section_id JOIN course c ON c.course_id = cs.course_id WHERE c.course_name LIKE :kw ORDER BY c.course_name LIMIT 20"
                : "SELECT DISTINCT c.course_name FROM schedule s JOIN class_section cs ON cs.section_id = s.section_id JOIN course c ON c.course_id = cs.course_id ORDER BY c.course_name LIMIT 20";

        var query = em.createNativeQuery(sql);
        if (keyword != null && !keyword.isBlank())
            query.setParameter("kw", "%" + keyword.trim() + "%");

        @SuppressWarnings("unchecked")
        List<String> result = query.getResultList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/v1/admin/schedules/suggest/lecturers?keyword=Nguyen
     * Trả về danh sách tên giảng viên distinct đang có trong schedule.
     */
    @GetMapping("/api/v1/admin/schedules/suggest/lecturers")
    public ResponseEntity<ApiResponse<List<String>>> suggestLecturers(
            @RequestParam(required = false) String keyword) {

        String sql = keyword != null && !keyword.isBlank()
                ? "SELECT DISTINCT lecturer_name FROM schedule WHERE lecturer_name LIKE :kw AND lecturer_name IS NOT NULL ORDER BY lecturer_name LIMIT 20"
                : "SELECT DISTINCT lecturer_name FROM schedule WHERE lecturer_name IS NOT NULL ORDER BY lecturer_name LIMIT 20";

        var query = em.createNativeQuery(sql);
        if (keyword != null && !keyword.isBlank())
            query.setParameter("kw", "%" + keyword.trim() + "%");

        @SuppressWarnings("unchecked")
        List<String> result = query.getResultList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/v1/admin/schedules/suggest/rooms?keyword=A1
     * Trả về danh sách phòng distinct đang có trong schedule.
     */
    @GetMapping("/api/v1/admin/schedules/suggest/rooms")
    public ResponseEntity<ApiResponse<List<String>>> suggestRooms(
            @RequestParam(required = false) String keyword) {

        String sql = keyword != null && !keyword.isBlank()
                ? "SELECT DISTINCT room FROM schedule WHERE room LIKE :kw AND room IS NOT NULL ORDER BY room LIMIT 20"
                : "SELECT DISTINCT room FROM schedule WHERE room IS NOT NULL ORDER BY room LIMIT 20";

        var query = em.createNativeQuery(sql);
        if (keyword != null && !keyword.isBlank())
            query.setParameter("kw", "%" + keyword.trim() + "%");

        @SuppressWarnings("unchecked")
        List<String> result = query.getResultList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    // ════════════════════════════════════════════════════════════════════════
    // SECTION SEARCH — autocomplete cho form tạo mới lịch
    // ════════════════════════════════════════════════════════════════════════

    @PersistenceContext
    private EntityManager em;

    /**
     * GET /api/v1/admin/sections?keyword=MATH101&semesterId=1
     * Tìm lớp học phần theo section_code. Tối đa 20 kết quả.
     */
    @GetMapping("/api/v1/admin/sections")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchSections(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long semesterId) {

        StringBuilder sql = new StringBuilder("""
                SELECT cs.section_id, cs.section_code,
                       c.course_code, c.course_name,
                       sem.semester_name
                FROM class_section cs
                JOIN course c ON c.course_id = cs.course_id
                JOIN semester sem ON sem.semester_id = cs.semester_id
                WHERE 1=1
                """);

        if (keyword != null && !keyword.isBlank())
            sql.append(" AND cs.section_code LIKE :kw");
        if (semesterId != null)
            sql.append(" AND cs.semester_id = :semId");

        sql.append(" ORDER BY cs.section_code LIMIT 20");

        var query = em.createNativeQuery(sql.toString());
        if (keyword != null && !keyword.isBlank())
            query.setParameter("kw", "%" + keyword.trim() + "%");
        if (semesterId != null)
            query.setParameter("semId", semesterId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        List<Map<String, Object>> result = rows.stream().map(r -> Map.<String, Object>of(
                "sectionId", ((Number) r[0]).longValue(),
                "sectionCode", r[1],
                "courseCode", r[2],
                "courseName", r[3],
                "semesterName", r[4])).toList();

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
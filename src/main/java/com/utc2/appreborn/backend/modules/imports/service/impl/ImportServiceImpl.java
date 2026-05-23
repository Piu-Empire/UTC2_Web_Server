package com.utc2.appreborn.backend.modules.imports.service.impl;

import com.utc2.appreborn.backend.modules.auth.entity.User;
import com.utc2.appreborn.backend.common.enums.Role;
import com.utc2.appreborn.backend.modules.auth.repository.UserRepository;
import com.utc2.appreborn.backend.modules.finance.entity.TuitionFee;
import com.utc2.appreborn.backend.modules.finance.repository.TuitionFeeRepository;
import com.utc2.appreborn.backend.modules.imports.dto.*;
import com.utc2.appreborn.backend.modules.imports.service.ImportService;
import com.utc2.appreborn.backend.modules.profile.entity.StudentProfileEntity;
import com.utc2.appreborn.backend.modules.profile.entity.UserProfileEntity;
import com.utc2.appreborn.backend.modules.profile.repository.StudentProfileRepository;
import com.utc2.appreborn.backend.modules.profile.repository.UserProfileRepository;
import com.utc2.appreborn.backend.modules.academic.entity.CourseEntity;
import com.utc2.appreborn.backend.modules.academic.repository.CourseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportServiceImpl implements ImportService {

    private final UserRepository           userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserProfileRepository    userProfileRepository;
    private final TuitionFeeRepository     tuitionFeeRepository;
    private final CourseRepository courseRepository;
    @PersistenceContext
    private EntityManager em;

    // ─────────────────────────────────────────────────────────
    // IMPORT PROFILE
    // Required col: student_code
    // Optional: full_name, phone_number, date_of_birth, gender,
    //           address, faculty, major, academic_year, class_name, status
    // ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ImportResultResponse importProfiles(MultipartFile file, boolean overwrite) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        int success = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> colMap = buildColMap(sheet.getRow(0));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                int rowNum = i + 1;
                try {
                    String studentCode  = getCol(row, colMap, "student_code");
                    String fullName     = getCol(row, colMap, "full_name");
                    String phone        = getCol(row, colMap, "phone_number");
                    String dob          = getCol(row, colMap, "date_of_birth");
                    String gender       = getCol(row, colMap, "gender");
                    String address      = getCol(row, colMap, "address");
                    String faculty      = getCol(row, colMap, "faculty");
                    String major        = getCol(row, colMap, "major");
                    String academicYear = getCol(row, colMap, "academic_year");
                    String className    = getCol(row, colMap, "class_name");
                    String status       = getCol(row, colMap, "status");

                    if (studentCode.isEmpty()) {
                        errors.add(err(rowNum, "student_code", "Không được để trống"));
                        continue;
                    }

                    Optional<StudentProfileEntity> spOpt = studentProfileRepository.findByStudentCode(studentCode);
                    if (spOpt.isEmpty()) {
                        errors.add(err(rowNum, "student_code", "Không tìm thấy sinh viên: " + studentCode));
                        continue;
                    }

                    StudentProfileEntity sp = spOpt.get();
                    Long userId = sp.getUserId();

                    if (!faculty.isEmpty())      sp.setFaculty(faculty);
                    if (!major.isEmpty())        sp.setMajor(major);
                    if (!academicYear.isEmpty()) sp.setAcademicYear(academicYear);
                    if (!className.isEmpty())    sp.setClassName(className);
                    if (!status.isEmpty())       sp.setStatus(status);
                    studentProfileRepository.save(sp);

                    UserProfileEntity up = userProfileRepository.findById(userId)
                            .orElse(UserProfileEntity.builder().userId(userId).user(sp.getUser()).build());

                    if (!fullName.isEmpty()) up.setFullName(fullName);
                    if (!phone.isEmpty())    up.setPhoneNumber(phone);
                    if (!gender.isEmpty())   up.setGender(gender);
                    if (!address.isEmpty())  up.setAddress(address);

                    // date_of_birth — ưu tiên Date cell, fallback String
                    Integer dobIdx = colMap.get("date_of_birth");
                    if (dobIdx != null) {
                        Cell dobCell = row.getCell(dobIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        if (dobCell != null) {
                            if (dobCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dobCell)) {
                                up.setDateOfBirth(dobCell.getDateCellValue()
                                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                            } else if (!dob.isEmpty()) {
                                try { up.setDateOfBirth(LocalDate.parse(dob)); }
                                catch (Exception e) {
                                    errors.add(err(rowNum, "date_of_birth", "Sai định dạng ngày (yyyy-MM-dd)"));
                                }
                            }
                        }
                    }

                    userProfileRepository.save(up);
                    success++;

                } catch (Exception e) {
                    errors.add(err(rowNum, "unknown", e.getMessage()));
                }
            }
        } catch (Exception e) {
            errors.add(err(0, "file", "Lỗi đọc file: " + e.getMessage()));
        }

        return ImportResultResponse.builder()
                .success(success).failed(errors.size()).errors(errors).build();
    }

    // ─────────────────────────────────────────────────────────
    // IMPORT TUITION
    // Required cols: student_code, semester_id, total_amount
    // Optional: semester_name (ignored), paid_amount, due_date, payment_method
    // ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ImportResultResponse importTuition(MultipartFile file, boolean overwrite) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        int success = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> colMap = buildColMap(sheet.getRow(0));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                int rowNum = i + 1;
                try {
                    String studentCode    = getCol(row, colMap, "student_code");
                    String semesterIdStr  = getCol(row, colMap, "semester_id");
                    String totalAmountStr = getCol(row, colMap, "total_amount");
                    String paidAmountStr  = getCol(row, colMap, "paid_amount");
                    String paymentMethod  = getCol(row, colMap, "payment_method");

                    if (studentCode.isEmpty()) {
                        errors.add(err(rowNum, "student_code", "Không được để trống")); continue;
                    }
                    if (semesterIdStr.isEmpty()) {
                        errors.add(err(rowNum, "semester_id", "Không được để trống")); continue;
                    }
                    if (totalAmountStr.isEmpty()) {
                        errors.add(err(rowNum, "total_amount", "Không được để trống")); continue;
                    }

                    Optional<StudentProfileEntity> spOpt = studentProfileRepository.findByStudentCode(studentCode);
                    if (spOpt.isEmpty()) {
                        errors.add(err(rowNum, "student_code", "Không tìm thấy sinh viên: " + studentCode)); continue;
                    }

                    Long userId     = spOpt.get().getUserId();
                    Long semesterId = Long.parseLong(semesterIdStr);
                    BigDecimal totalAmount = new BigDecimal(totalAmountStr.replace(",", ""));
                    BigDecimal paidAmount  = paidAmountStr.isEmpty()
                            ? BigDecimal.ZERO
                            : new BigDecimal(paidAmountStr.replace(",", ""));
                    BigDecimal remaining = totalAmount.subtract(paidAmount);

                    Optional<TuitionFee> existing = tuitionFeeRepository.findByUserIdAndSemesterId(userId, semesterId);
                    TuitionFee fee;
                    if (existing.isPresent() && overwrite) {
                        fee = existing.get();
                    } else if (existing.isPresent()) {
                        errors.add(err(rowNum, "semester_id",
                                "Đã tồn tại học phí kỳ " + semesterId + " — dùng overwrite=true để ghi đè"));
                        continue;
                    } else {
                        User user = userRepository.findById(userId).orElseThrow();
                        fee = TuitionFee.builder().user(user).semesterId(semesterId).build();
                    }

                    fee.setTotalAmount(totalAmount);
                    fee.setPaidAmount(paidAmount);
                    fee.setRemainingAmount(remaining);

                    if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
                        fee.setStatus("chưa đóng");
                    } else if (paidAmount.compareTo(totalAmount) >= 0) {
                        fee.setStatus("đã đóng đủ");
                        fee.setPaidAt(LocalDateTime.now());
                    } else {
                        fee.setStatus("đóng một phần");
                    }

                    // due_date — ưu tiên Date cell, fallback String
                    Integer dueDateIdx = colMap.get("due_date");
                    if (dueDateIdx != null) {
                        Cell dueDateCell = row.getCell(dueDateIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        if (dueDateCell != null) {
                            if (dueDateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dueDateCell)) {
                                fee.setDueDate(dueDateCell.getDateCellValue()
                                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                            } else {
                                String dueDateStr = getString(row, dueDateIdx);
                                if (!dueDateStr.isEmpty()) {
                                    try { fee.setDueDate(LocalDate.parse(dueDateStr)); }
                                    catch (Exception e) {
                                        errors.add(err(rowNum, "due_date", "Sai định dạng ngày (yyyy-MM-dd)")); continue;
                                    }
                                }
                            }
                        }
                    }

                    if (!paymentMethod.isEmpty()) fee.setPaymentMethod(paymentMethod);

                    tuitionFeeRepository.save(fee);
                    success++;

                } catch (Exception e) {
                    errors.add(err(rowNum, "unknown", e.getMessage()));
                }
            }
        } catch (Exception e) {
            errors.add(err(0, "file", "Lỗi đọc file: " + e.getMessage()));
        }

        return ImportResultResponse.builder()
                .success(success).failed(errors.size()).errors(errors).build();
    }

    // ─────────────────────────────────────────────────────────
    // IMPORT CURRICULUM
    // ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ImportResultResponse importCurriculum(MultipartFile file, boolean overwrite) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        int success = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> colMap = buildColMap(sheet.getRow(0));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                int rowNum = i + 1;
                try {
                    String major              = getCol(row, colMap, "major");
                    String academicYear       = getCol(row, colMap, "academic_year");
                    String courseCode         = getCol(row, colMap, "course_code");
                    String semesterSuggestion = getCol(row, colMap, "semester_suggestion");
                    String isRequiredStr      = getCol(row, colMap, "is_required");
                    String groupName          = getCol(row, colMap, "group_name");

                    if (major.isEmpty() || academicYear.isEmpty() || courseCode.isEmpty() || semesterSuggestion.isEmpty()) {
                        errors.add(err(rowNum, "required", "major, academic_year, course_code, semester_suggestion là bắt buộc"));
                        continue;
                    }

                    List<?> curriculumIds = em.createNativeQuery(
                            "SELECT curriculum_id FROM curriculum WHERE major=? AND academic_year=?")
                            .setParameter(1, major).setParameter(2, academicYear).getResultList();

                    Long curriculumId;
                    if (curriculumIds.isEmpty()) {
                        em.createNativeQuery(
                            "INSERT INTO curriculum (major, academic_year, total_credits_required, description) VALUES (?, ?, 0, ?)")
                            .setParameter(1, major).setParameter(2, academicYear)
                            .setParameter(3, "Chương trình đào tạo " + major + " " + academicYear)
                            .executeUpdate();
                        // Query lại thay vì dùng LAST_INSERT_ID() vì JPA có thể dùng connection khác
                        List<?> newIds = em.createNativeQuery(
                            "SELECT curriculum_id FROM curriculum WHERE major=? AND academic_year=?")
                            .setParameter(1, major).setParameter(2, academicYear).getResultList();
                        curriculumId = ((Number) newIds.get(0)).longValue();
                    } else {
                        curriculumId = ((Number) curriculumIds.get(0)).longValue();
                    }

                    List<?> courseIds = em.createNativeQuery("SELECT course_id FROM course WHERE course_code=?")
                            .setParameter(1, courseCode).getResultList();
                    if (courseIds.isEmpty()) {
                        errors.add(err(rowNum, "course_code", "Không tìm thấy học phần: " + courseCode)); continue;
                    }

                    Long courseId  = ((Number) courseIds.get(0)).longValue();
                    int semSugg    = Integer.parseInt(semesterSuggestion);
                    boolean isReq  = !"false".equalsIgnoreCase(isRequiredStr);

                    List<?> existing = em.createNativeQuery(
                            "SELECT 1 FROM curriculum_item WHERE curriculum_id=? AND course_id=?")
                            .setParameter(1, curriculumId).setParameter(2, courseId).getResultList();

                    if (!existing.isEmpty()) {
                        if (overwrite) {
                            em.createNativeQuery(
                                "UPDATE curriculum_item SET semester_suggestion=?, is_required=?, group_name=? WHERE curriculum_id=? AND course_id=?")
                                .setParameter(1, semSugg).setParameter(2, isReq)
                                .setParameter(3, groupName.isEmpty() ? null : groupName)
                                .setParameter(4, curriculumId).setParameter(5, courseId).executeUpdate();
                        } else {
                            errors.add(err(rowNum, "course_code", "Học phần " + courseCode + " đã có trong CTĐT — dùng overwrite=true"));
                            continue;
                        }
                    } else {
                        em.createNativeQuery(
                            "INSERT INTO curriculum_item (curriculum_id, course_id, semester_suggestion, is_required, group_name) VALUES (?, ?, ?, ?, ?)")
                            .setParameter(1, curriculumId).setParameter(2, courseId)
                            .setParameter(3, semSugg).setParameter(4, isReq)
                            .setParameter(5, groupName.isEmpty() ? null : groupName).executeUpdate();
                    }

                    success++;
                } catch (Exception e) {
                    errors.add(err(rowNum, "unknown", e.getMessage()));
                }
            }
        } catch (Exception e) {
            errors.add(err(0, "file", "Lỗi đọc file: " + e.getMessage()));
        }

        return ImportResultResponse.builder()
                .success(success).failed(errors.size()).errors(errors).build();
    }

    // ─── Helpers ──────────────────────────────────────────────

    /**
     * Đọc header row (row 0) và build map: tên_cột_lowercase → index.
     * Tự động bỏ qua các cột trống hoặc null.
     */
    private Map<String, Integer> buildColMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null) return map;
        for (int c = headerRow.getFirstCellNum(); c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null) continue;
            String name = getString(headerRow, c).toLowerCase().trim();
            if (!name.isEmpty()) map.put(name, c);
        }
        return map;
    }

    /**
     * Lấy giá trị cột theo tên (case-insensitive). Trả về "" nếu cột không tồn tại.
     */
    private String getCol(Row row, Map<String, Integer> colMap, String colName) {
        Integer idx = colMap.get(colName.toLowerCase());
        if (idx == null) return "";
        return getString(row, idx);
    }

    /**
     * Đọc cell thành String — xử lý Numeric/String/Boolean/Formula/Date.
     * Numeric không phải date → bỏ ".0" nếu là số nguyên.
     */
    private String getString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue()
                            .toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
                }
                double v = cell.getNumericCellValue();
                return (v == Math.floor(v)) ? String.valueOf((long) v) : String.valueOf(v);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try { return String.valueOf((long) cell.getNumericCellValue()); }
                catch (Exception e) { return cell.getStringCellValue().trim(); }
            default:
                return "";
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !cell.toString().trim().isEmpty()) return false;
        }
        return true;
    }

    private ImportResultResponse.ImportError err(int row, String field, String msg) {
        return ImportResultResponse.ImportError.builder()
                .row(row).field(field).message(msg).build();
    }

    // ─────────────────────────────────────────────────────────
    // IMPORT COURSES (học phần)
    // Required cols: course_code, course_name, credits
    // Optional: description
    // ─────────────────────────────────────────────────────────
   @Override
    @Transactional
    public ImportResultResponse importCourses(MultipartFile file, boolean overwrite) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        int success = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> colMap = buildColMap(sheet.getRow(0));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                int rowNum = i + 1;
                try {
                    String courseCode = getCol(row, colMap, "course_code").trim();
                    String courseName = getCol(row, colMap, "course_name").trim();
                    String creditsStr = getCol(row, colMap, "credits").trim();
                    String desc       = getCol(row, colMap, "description").trim();

                    if (courseCode.isEmpty() || courseName.isEmpty() || creditsStr.isEmpty()) {
                        errors.add(err(rowNum, "validation", "Thiếu dữ liệu bắt buộc"));
                        continue;
                    }

                    int credits;
                    try {
                        credits = Integer.parseInt(creditsStr);
                    } catch (NumberFormatException e) {
                        errors.add(err(rowNum, "credits", "Credits phải là số"));
                        continue;
                    }

                    // Sử dụng CourseEntity thay cho Course
                    Optional<CourseEntity> existing = courseRepository.findByCourseCode(courseCode);

                    if (existing.isPresent()) {
                        if (overwrite) {
                            CourseEntity c = existing.get();
                            c.setCourseName(courseName);
                            c.setCredits(credits);
                            c.setDescription(desc.isEmpty() ? null : desc);
                            courseRepository.save(c);
                        } else {
                            errors.add(err(rowNum, "course_code", "Học phần đã tồn tại"));
                            continue;
                        }
                    } else {
                        CourseEntity newCourse = new CourseEntity();
                        newCourse.setCourseCode(courseCode);
                        newCourse.setCourseName(courseName);
                        newCourse.setCredits(credits);
                        newCourse.setDescription(desc.isEmpty() ? null : desc);
                        courseRepository.save(newCourse);
                    }
                    success++;
                } catch (Exception e) {
                    errors.add(err(rowNum, "system", e.getMessage()));
                }
            }
        } catch (Exception e) {
            errors.add(err(0, "file", "Lỗi đọc file: " + e.getMessage()));
        }
        return ImportResultResponse.builder()
                .success(success)
                .failed(errors.size())
                .errors(errors)
                .build();
    }

// Hàm bổ trợ để parse số an toàn
private Integer parseInteger(String val) {
    if (val == null || val.isEmpty()) return null;
    try { return Integer.parseInt(val); }
    catch (NumberFormatException e) { return null; }
}
    // ─────────────────────────────────────────────────────────
    // IMPORT STUDENTS (tạo tài khoản sinh viên mới)
    // Required cols: email, full_name, student_code, faculty, major, academic_year, status
    // Optional: phone_number, date_of_birth, gender, address, class_name
    // ─────────────────────────────────────────────────────────
    @Override
    public ImportResultResponse importStudents(MultipartFile file, boolean overwrite) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        int success = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> colMap = buildColMap(sheet.getRow(0));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                int rowNum = i + 1;
                try {
                    String email       = getCol(row, colMap, "email");
                    String fullName    = getCol(row, colMap, "full_name");
                    String studentCode = getCol(row, colMap, "student_code");
                    String faculty     = getCol(row, colMap, "faculty");
                    String major       = getCol(row, colMap, "major");
                    String academicYear= getCol(row, colMap, "academic_year");
                    String status      = getCol(row, colMap, "status");
                    String phone       = getCol(row, colMap, "phone_number");
                    String dob         = getCol(row, colMap, "date_of_birth");
                    String gender      = getCol(row, colMap, "gender");
                    String address     = getCol(row, colMap, "address");
                    String className   = getCol(row, colMap, "class_name");

                    // Validate required
                    if (email.isEmpty()) {
                        errors.add(err(rowNum, "email", "Không được để trống")); continue;
                    }
                    if (fullName.isEmpty()) {
                        errors.add(err(rowNum, "full_name", "Không được để trống")); continue;
                    }
                    if (studentCode.isEmpty()) {
                        errors.add(err(rowNum, "student_code", "Không được để trống")); continue;
                    }
                    if (faculty.isEmpty()) {
                        errors.add(err(rowNum, "faculty", "Không được để trống")); continue;
                    }
                    if (major.isEmpty()) {
                        errors.add(err(rowNum, "major", "Không được để trống")); continue;
                    }
                    if (academicYear.isEmpty()) {
                        errors.add(err(rowNum, "academic_year", "Không được để trống")); continue;
                    }

                    // Kiểm tra trùng student_code
                    Optional<StudentProfileEntity> existingSp = studentProfileRepository.findByStudentCode(studentCode);
                    if (existingSp.isPresent()) {
                        if (!overwrite) {
                            errors.add(err(rowNum, "student_code",
                                    "Sinh viên " + studentCode + " đã tồn tại — dùng overwrite=true để ghi đè"));
                            continue;
                        }
                        // overwrite: cập nhật profile + user profile
                        StudentProfileEntity sp = existingSp.get();
                        sp.setFaculty(faculty); sp.setMajor(major);
                        sp.setAcademicYear(academicYear);
                        if (!className.isEmpty()) sp.setClassName(className);
                        if (!status.isEmpty())    sp.setStatus(status);
                        studentProfileRepository.saveAndFlush(sp);

                        Long userId = sp.getUserId();
                        UserProfileEntity up = userProfileRepository.findById(userId)
                                .orElse(UserProfileEntity.builder().userId(userId).user(sp.getUser()).build());
                        if (!fullName.isEmpty()) up.setFullName(fullName);
                        if (!phone.isEmpty())    up.setPhoneNumber(phone);
                        if (!gender.isEmpty())   up.setGender(gender);
                        if (!address.isEmpty())  up.setAddress(address);
                        parseDob(row, colMap, dob, up, errors, rowNum);
                        userProfileRepository.saveAndFlush(up);
                        success++;
                        continue;
                    }

                    // Kiểm tra trùng email
                    if (userRepository.findByEmail(email).isPresent()) {
                        errors.add(err(rowNum, "email", "Email " + email + " đã được sử dụng")); continue;
                    }

                    // Tạo User mới với password mặc định = student_code
                    User user = User.builder()
                            .email(email)
                            .password(studentCode)   // sẽ encode ở service layer nếu có PasswordEncoder
                            .role(Role.STUDENT)
                            .enabled(true)
                            .build();
                    user = userRepository.saveAndFlush(user);

                    // Tạo StudentProfile
                    StudentProfileEntity sp = StudentProfileEntity.builder()
                            .userId(user.getId())
                            .user(user)
                            .studentCode(studentCode)
                            .faculty(faculty)
                            .major(major)
                            .academicYear(academicYear)
                            .className(className.isEmpty() ? null : className)
                            .status(status.isEmpty() ? "active" : status)
                            .build();
                    studentProfileRepository.saveAndFlush(sp);

                    // Tạo UserProfile
                    UserProfileEntity up = UserProfileEntity.builder()
                            .userId(user.getId())
                            .user(user)
                            .fullName(fullName)
                            .phoneNumber(phone.isEmpty() ? null : phone)
                            .gender(gender.isEmpty() ? null : gender)
                            .address(address.isEmpty() ? null : address)
                            .build();
                    parseDob(row, colMap, dob, up, errors, rowNum);
                    userProfileRepository.saveAndFlush(up);

                    success++;
                } catch (Exception e) {
                    errors.add(err(rowNum, "unknown", e.getMessage()));
                }
            }
        } catch (Exception e) {
            errors.add(err(0, "file", "Lỗi đọc file: " + e.getMessage()));
        }

        return ImportResultResponse.builder()
                .success(success).failed(errors.size()).errors(errors).build();
    }

    /** Helper: parse date_of_birth vào UserProfile, hỗ trợ Date cell và String yyyy-MM-dd */
    private void parseDob(Row row, Map<String, Integer> colMap, String dobStr,
                          UserProfileEntity up, List<ImportResultResponse.ImportError> errors, int rowNum) {
        Integer dobIdx = colMap.get("date_of_birth");
        if (dobIdx == null) return;
        Cell dobCell = row.getCell(dobIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (dobCell == null) return;
        if (dobCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dobCell)) {
            up.setDateOfBirth(dobCell.getDateCellValue()
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        } else if (!dobStr.isEmpty()) {
            try { up.setDateOfBirth(LocalDate.parse(dobStr)); }
            catch (Exception e) {
                errors.add(err(rowNum, "date_of_birth", "Sai định dạng ngày (yyyy-MM-dd)"));
            }
        }
    }
}
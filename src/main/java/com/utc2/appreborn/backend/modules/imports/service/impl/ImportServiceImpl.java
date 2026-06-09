package com.utc2.appreborn.backend.modules.imports.service.impl;

import com.utc2.appreborn.backend.modules.auth.entity.User;
import com.utc2.appreborn.backend.common.enums.Role;
import com.utc2.appreborn.backend.modules.auth.repository.UserRepository;
import com.utc2.appreborn.backend.modules.dormitory.entity.DormitoryRoomEntity;
import com.utc2.appreborn.backend.modules.dormitory.repository.DormitoryRoomRepository;
import com.utc2.appreborn.backend.modules.enrollment.entity.EnrollmentEntity;
import com.utc2.appreborn.backend.modules.enrollment.repository.CourseEnrollmentRepository;
import com.utc2.appreborn.backend.modules.finance.entity.TuitionFee;
import com.utc2.appreborn.backend.modules.finance.repository.TuitionFeeRepository;
import com.utc2.appreborn.backend.modules.imports.dto.*;
import com.utc2.appreborn.backend.modules.imports.service.ImportService;
import com.utc2.appreborn.backend.modules.profile.entity.StudentProfileEntity;
import com.utc2.appreborn.backend.modules.profile.entity.UserProfileEntity;
import com.utc2.appreborn.backend.modules.profile.repository.StudentProfileRepository;
import com.utc2.appreborn.backend.modules.profile.repository.UserProfileRepository;
import com.utc2.appreborn.backend.modules.academic.entity.CourseEntity;
import com.utc2.appreborn.backend.modules.academic.entity.AcademicWarningEntity;
import com.utc2.appreborn.backend.modules.academic.entity.ScholarshipEntity;
import com.utc2.appreborn.backend.modules.academic.repository.CourseRepository;
import com.utc2.appreborn.backend.modules.academic.repository.AcademicWarningRepository;
import com.utc2.appreborn.backend.modules.academic.repository.ScholarshipRepository;
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

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserProfileRepository userProfileRepository;
    private final TuitionFeeRepository tuitionFeeRepository;
    private final CourseRepository courseRepository;
    private final DormitoryRoomRepository dormitoryRoomRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final AcademicWarningRepository academicWarningRepository;

    @PersistenceContext
    private EntityManager em;

    // ─────────────────────────────────────────────────────────
    // IMPORT PROFILE
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
                if (row == null || isRowEmpty(row))
                    continue;

                int rowNum = i + 1;
                try {
                    String studentCode = getCol(row, colMap, "student_code");
                    String fullName = getCol(row, colMap, "full_name");
                    String phone = getCol(row, colMap, "phone_number");
                    String dob = getCol(row, colMap, "date_of_birth");
                    String gender = getCol(row, colMap, "gender");
                    String address = getCol(row, colMap, "address");
                    String faculty = getCol(row, colMap, "faculty");
                    String major = getCol(row, colMap, "major");
                    String academicYear = getCol(row, colMap, "academic_year");
                    String className = getCol(row, colMap, "class_name");
                    String status = getCol(row, colMap, "status");

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

                    if (!faculty.isEmpty())
                        sp.setFaculty(faculty);
                    if (!major.isEmpty())
                        sp.setMajor(major);
                    if (!academicYear.isEmpty())
                        sp.setAcademicYear(academicYear);
                    if (!className.isEmpty())
                        sp.setClassName(className);
                    if (!status.isEmpty())
                        sp.setStatus(status);
                    studentProfileRepository.save(sp);

                    UserProfileEntity up = userProfileRepository.findById(userId)
                            .orElse(UserProfileEntity.builder().userId(userId).user(sp.getUser()).build());

                    if (!fullName.isEmpty())
                        up.setFullName(fullName);
                    if (!phone.isEmpty())
                        up.setPhoneNumber(phone);
                    if (!gender.isEmpty())
                        up.setGender(gender);
                    if (!address.isEmpty())
                        up.setAddress(address);

                    Integer dobIdx = colMap.get("date_of_birth");
                    if (dobIdx != null) {
                        Cell dobCell = row.getCell(dobIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        if (dobCell != null) {
                            if (dobCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dobCell)) {
                                up.setDateOfBirth(dobCell.getDateCellValue()
                                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                            } else if (!dob.isEmpty()) {
                                try {
                                    up.setDateOfBirth(LocalDate.parse(dob));
                                } catch (Exception e) {
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
                if (row == null || isRowEmpty(row))
                    continue;

                int rowNum = i + 1;
                try {
                    String studentCode = getCol(row, colMap, "student_code");
                    String semesterIdStr = getCol(row, colMap, "semester_id");
                    String totalAmountStr = getCol(row, colMap, "total_amount");
                    String paidAmountStr = getCol(row, colMap, "paid_amount");
                    String paymentMethod = getCol(row, colMap, "payment_method");

                    if (studentCode.isEmpty()) {
                        errors.add(err(rowNum, "student_code", "Không được để trống"));
                        continue;
                    }
                    if (semesterIdStr.isEmpty()) {
                        errors.add(err(rowNum, "semester_id", "Không được để trống"));
                        continue;
                    }
                    if (totalAmountStr.isEmpty()) {
                        errors.add(err(rowNum, "total_amount", "Không được để trống"));
                        continue;
                    }

                    Optional<StudentProfileEntity> spOpt = studentProfileRepository.findByStudentCode(studentCode);
                    if (spOpt.isEmpty()) {
                        errors.add(err(rowNum, "student_code", "Không tìm thấy sinh viên: " + studentCode));
                        continue;
                    }

                    Long userId = spOpt.get().getUserId();
                    Long semesterId = Long.parseLong(semesterIdStr);
                    BigDecimal totalAmount = new BigDecimal(totalAmountStr.replace(",", ""));
                    BigDecimal paidAmount = paidAmountStr.isEmpty()
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

                    Integer dueDateIdx = colMap.get("due_date");
                    if (dueDateIdx != null) {
                        Cell dueDateCell = row.getCell(dueDateIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        if (dueDateCell != null) {
                            if (dueDateCell.getCellType() == CellType.NUMERIC
                                    && DateUtil.isCellDateFormatted(dueDateCell)) {
                                fee.setDueDate(dueDateCell.getDateCellValue()
                                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                            } else {
                                String dueDateStr = getString(row, dueDateIdx);
                                if (!dueDateStr.isEmpty()) {
                                    try {
                                        fee.setDueDate(LocalDate.parse(dueDateStr));
                                    } catch (Exception e) {
                                        errors.add(err(rowNum, "due_date", "Sai định dạng ngày (yyyy-MM-dd)"));
                                        continue;
                                    }
                                }
                            }
                        }
                    }

                    if (!paymentMethod.isEmpty())
                        fee.setPaymentMethod(paymentMethod);

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
                if (row == null || isRowEmpty(row))
                    continue;

                int rowNum = i + 1;
                try {
                    String major = getCol(row, colMap, "major");
                    String academicYear = getCol(row, colMap, "academic_year");
                    String courseCode = getCol(row, colMap, "course_code");
                    String semesterSuggestion = getCol(row, colMap, "semester_suggestion");
                    String isRequiredStr = getCol(row, colMap, "is_required");
                    String groupName = getCol(row, colMap, "group_name");

                    if (major.isEmpty() || academicYear.isEmpty() || courseCode.isEmpty()
                            || semesterSuggestion.isEmpty()) {
                        errors.add(err(rowNum, "required",
                                "major, academic_year, course_code, semester_suggestion là bắt buộc"));
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
                        errors.add(err(rowNum, "course_code", "Không tìm thấy học phần: " + courseCode));
                        continue;
                    }

                    Long courseId = ((Number) courseIds.get(0)).longValue();
                    int semSugg = Integer.parseInt(semesterSuggestion);
                    boolean isReq = !"false".equalsIgnoreCase(isRequiredStr);

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
                            errors.add(err(rowNum, "course_code",
                                    "Học phần " + courseCode + " đã có trong CTĐT — dùng overwrite=true"));
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

    // ─────────────────────────────────────────────────────────
    // IMPORT COURSES
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
                if (row == null || isRowEmpty(row))
                    continue;

                int rowNum = i + 1;
                try {
                    String courseCode = getCol(row, colMap, "course_code").trim();
                    String courseName = getCol(row, colMap, "course_name").trim();
                    String creditsStr = getCol(row, colMap, "credits").trim();
                    String desc = getCol(row, colMap, "description").trim();

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
                .success(success).failed(errors.size()).errors(errors).build();
    }

    // ─────────────────────────────────────────────────────────
    // IMPORT STUDENTS
    // ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ImportResultResponse importStudents(MultipartFile file, boolean overwrite) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        int success = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> colMap = buildColMap(sheet.getRow(0));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row))
                    continue;

                int rowNum = i + 1;
                try {
                    String email = getCol(row, colMap, "email");
                    String fullName = getCol(row, colMap, "full_name");
                    String studentCode = getCol(row, colMap, "student_code");
                    String faculty = getCol(row, colMap, "faculty");
                    String major = getCol(row, colMap, "major");
                    String academicYear = getCol(row, colMap, "academic_year");
                    String status = getCol(row, colMap, "status");
                    String phone = getCol(row, colMap, "phone_number");
                    String dob = getCol(row, colMap, "date_of_birth");
                    String gender = getCol(row, colMap, "gender");
                    String address = getCol(row, colMap, "address");
                    String className = getCol(row, colMap, "class_name");

                    if (email.isEmpty()) {
                        errors.add(err(rowNum, "email", "Không được để trống"));
                        continue;
                    }
                    if (fullName.isEmpty()) {
                        errors.add(err(rowNum, "full_name", "Không được để trống"));
                        continue;
                    }
                    if (studentCode.isEmpty()) {
                        errors.add(err(rowNum, "student_code", "Không được để trống"));
                        continue;
                    }
                    if (faculty.isEmpty()) {
                        errors.add(err(rowNum, "faculty", "Không được để trống"));
                        continue;
                    }
                    if (major.isEmpty()) {
                        errors.add(err(rowNum, "major", "Không được để trống"));
                        continue;
                    }
                    if (academicYear.isEmpty()) {
                        errors.add(err(rowNum, "academic_year", "Không được để trống"));
                        continue;
                    }

                    Optional<StudentProfileEntity> existingSp = studentProfileRepository.findByStudentCode(studentCode);
                    if (existingSp.isPresent()) {
                        if (!overwrite) {
                            errors.add(err(rowNum, "student_code",
                                    "Sinh viên " + studentCode + " đã tồn tại — dùng overwrite=true để ghi đè"));
                            continue;
                        }
                        StudentProfileEntity sp = existingSp.get();
                        sp.setFaculty(faculty);
                        sp.setMajor(major);
                        sp.setAcademicYear(academicYear);
                        if (!className.isEmpty())
                            sp.setClassName(className);
                        if (!status.isEmpty())
                            sp.setStatus(status);
                        studentProfileRepository.saveAndFlush(sp);

                        Long userId = sp.getUserId();
                        UserProfileEntity up = userProfileRepository.findById(userId)
                                .orElse(UserProfileEntity.builder().userId(userId).user(sp.getUser()).build());
                        if (!fullName.isEmpty())
                            up.setFullName(fullName);
                        if (!phone.isEmpty())
                            up.setPhoneNumber(phone);
                        if (!gender.isEmpty())
                            up.setGender(gender);
                        if (!address.isEmpty())
                            up.setAddress(address);
                        parseDob(row, colMap, dob, up, errors, rowNum);
                        userProfileRepository.saveAndFlush(up);
                        success++;
                        continue;
                    }

                    if (userRepository.findByEmail(email).isPresent()) {
                        errors.add(err(rowNum, "email", "Email " + email + " đã được sử dụng"));
                        continue;
                    }

                    User user = User.builder()
                            .email(email)
                            .password(studentCode)
                            .role(Role.STUDENT)
                            .enabled(true)
                            .build();
                    user = userRepository.saveAndFlush(user);

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

    // ─────────────────────────────────────────────────────────
    // IMPORT DORMITORY ROOMS ← MỚI
    // Required: room_code, building, capacity, room_type, price_per_month
    // Optional: floor, status (default: available), amenities
    // ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ImportResultResponse importDormitoryRooms(MultipartFile file, boolean overwrite) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        int success = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> colMap = buildColMap(sheet.getRow(0));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row))
                    continue;

                int rowNum = i + 1;
                try {
                    String roomCode = getCol(row, colMap, "room_code");
                    String building = getCol(row, colMap, "building");
                    String capacityStr = getCol(row, colMap, "capacity");
                    String roomType = getCol(row, colMap, "room_type");
                    String priceStr = getCol(row, colMap, "price_per_month");
                    String floorStr = getCol(row, colMap, "floor");
                    String status = getCol(row, colMap, "status");
                    String amenities = getCol(row, colMap, "amenities");

                    if (roomCode.isEmpty() || building.isEmpty() || capacityStr.isEmpty()
                            || roomType.isEmpty() || priceStr.isEmpty()) {
                        errors.add(err(rowNum, "required",
                                "room_code, building, capacity, room_type, price_per_month là bắt buộc"));
                        continue;
                    }

                    Optional<DormitoryRoomEntity> existing = dormitoryRoomRepository.findByRoomCode(roomCode);
                    DormitoryRoomEntity room;
                    if (existing.isPresent()) {
                        if (!overwrite) {
                            errors.add(err(rowNum, "room_code",
                                    "Phòng " + roomCode + " đã tồn tại — dùng overwrite=true để ghi đè"));
                            continue;
                        }
                        room = existing.get();
                    } else {
                        room = new DormitoryRoomEntity();
                        room.setCurrentOccupancy(0);
                    }

                    room.setRoomCode(roomCode);
                    room.setBuilding(building);
                    room.setCapacity(Integer.parseInt(capacityStr));
                    room.setRoomType(roomType);
                    room.setPricePerMonth(Double.parseDouble(priceStr.replace(",", "")));
                    // Normalize status: map English/raw → Vietnamese cho khớp với app
                    room.setStatus(normalizeRoomStatus(status));
                    if (!floorStr.isEmpty())
                        room.setFloor(Integer.parseInt(floorStr));
                    if (!amenities.isEmpty())
                        room.setAmenities(amenities);

                    dormitoryRoomRepository.save(room);
                    success++;

                } catch (NumberFormatException e) {
                    errors.add(err(rowNum, "format", "capacity/price/floor phải là số: " + e.getMessage()));
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
    // IMPORT ENROLLMENTS ← MỚI
    // Required: student_code, course_code, semester_id
    // Optional: status, midterm_score, final_score, assignment_score,
    // total_score, letter_grade, grade_point, is_passed
    // ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ImportResultResponse importEnrollments(MultipartFile file, boolean overwrite) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        int success = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> colMap = buildColMap(sheet.getRow(0));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row))
                    continue;

                int rowNum = i + 1;
                try {
                    String studentCode = getCol(row, colMap, "student_code");
                    String courseCode = getCol(row, colMap, "course_code");
                    String semIdStr = getCol(row, colMap, "semester_id");

                    if (studentCode.isEmpty() || courseCode.isEmpty() || semIdStr.isEmpty()) {
                        errors.add(err(rowNum, "required",
                                "student_code, course_code, semester_id là bắt buộc"));
                        continue;
                    }

                    // Resolve student → userId
                    Optional<StudentProfileEntity> spOpt = studentProfileRepository.findByStudentCode(studentCode);
                    if (spOpt.isEmpty()) {
                        errors.add(err(rowNum, "student_code", "Không tìm thấy sinh viên: " + studentCode));
                        continue;
                    }
                    Long userId = spOpt.get().getUserId();

                    // Resolve course → courseId
                    List<?> courseIds = em.createNativeQuery("SELECT course_id FROM course WHERE course_code=?")
                            .setParameter(1, courseCode).getResultList();
                    if (courseIds.isEmpty()) {
                        errors.add(err(rowNum, "course_code", "Không tìm thấy học phần: " + courseCode));
                        continue;
                    }
                    Long courseId = ((Number) courseIds.get(0)).longValue();
                    Long semId = Long.parseLong(semIdStr);

                    // Optional score fields
                    String status = getCol(row, colMap, "status");
                    String midtermStr = getCol(row, colMap, "midterm_score");
                    String finalStr = getCol(row, colMap, "final_score");
                    String assignmentStr = getCol(row, colMap, "assignment_score");
                    String totalStr = getCol(row, colMap, "total_score");
                    String letterGrade = getCol(row, colMap, "letter_grade");
                    String gradePointStr = getCol(row, colMap, "grade_point");
                    String isPassedStr = getCol(row, colMap, "is_passed");

                    // Check existing enrollment
                    boolean exists = courseEnrollmentRepository
                            .existsByUserIdAndCourseIdAndStatusNot(userId, courseId, "đã hủy");

                    if (exists) {
                        if (!overwrite) {
                            errors.add(err(rowNum, "enrollment",
                                    "Sinh viên " + studentCode + " đã đăng ký học phần " + courseCode
                                            + " — dùng overwrite=true"));
                            continue;
                        }
                        // Update enrollment scores via native query (EnrollmentEntity không có score
                        // fields)
                        em.createNativeQuery(
                                "UPDATE enrollment SET " +
                                        "status=?, midterm_score=?, final_score=?, assignment_score=?, " +
                                        "total_score=?, letter_grade=?, grade_point=?, is_passed=? " +
                                        "WHERE user_id=? AND course_id=? AND semester_id=?")
                                .setParameter(1, status.isEmpty() ? "registered" : status)
                                .setParameter(2, midtermStr.isEmpty() ? null : Double.parseDouble(midtermStr))
                                .setParameter(3, finalStr.isEmpty() ? null : Double.parseDouble(finalStr))
                                .setParameter(4, assignmentStr.isEmpty() ? null : Double.parseDouble(assignmentStr))
                                .setParameter(5, totalStr.isEmpty() ? null : Double.parseDouble(totalStr))
                                .setParameter(6, letterGrade.isEmpty() ? null : letterGrade)
                                .setParameter(7, gradePointStr.isEmpty() ? null : Double.parseDouble(gradePointStr))
                                .setParameter(8, isPassedStr.isEmpty() ? null : Boolean.parseBoolean(isPassedStr))
                                .setParameter(9, userId)
                                .setParameter(10, courseId)
                                .setParameter(11, semId)
                                .executeUpdate();
                    } else {
                        em.createNativeQuery(
                                "INSERT INTO enrollment " +
                                        "(user_id, course_id, semester_id, status, midterm_score, final_score, " +
                                        "assignment_score, total_score, letter_grade, grade_point, is_passed, registered_at) "
                                        +
                                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,NOW())")
                                .setParameter(1, userId)
                                .setParameter(2, courseId)
                                .setParameter(3, semId)
                                .setParameter(4, status.isEmpty() ? "registered" : status)
                                .setParameter(5, midtermStr.isEmpty() ? null : Double.parseDouble(midtermStr))
                                .setParameter(6, finalStr.isEmpty() ? null : Double.parseDouble(finalStr))
                                .setParameter(7, assignmentStr.isEmpty() ? null : Double.parseDouble(assignmentStr))
                                .setParameter(8, totalStr.isEmpty() ? null : Double.parseDouble(totalStr))
                                .setParameter(9, letterGrade.isEmpty() ? null : letterGrade)
                                .setParameter(10, gradePointStr.isEmpty() ? null : Double.parseDouble(gradePointStr))
                                .setParameter(11, isPassedStr.isEmpty() ? null : Boolean.parseBoolean(isPassedStr))
                                .executeUpdate();
                    }

                    success++;

                } catch (NumberFormatException e) {
                    errors.add(err(rowNum, "format", "semester_id/score phải là số: " + e.getMessage()));
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
    // HELPERS
    // ─────────────────────────────────────────────────────────

    private Map<String, Integer> buildColMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null)
            return map;
        for (int c = headerRow.getFirstCellNum(); c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null)
                continue;
            String name = getString(headerRow, c).toLowerCase().trim();
            if (!name.isEmpty())
                map.put(name, c);
        }
        return map;
    }

    private String getCol(Row row, Map<String, Integer> colMap, String colName) {
        Integer idx = colMap.get(colName.toLowerCase());
        if (idx == null)
            return "";
        return getString(row, idx);
    }

    private String getString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null)
            return "";
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
                try {
                    return String.valueOf((long) cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue().trim();
                }
            default:
                return "";
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !cell.toString().trim().isEmpty())
                return false;
        }
        return true;
    }

    private ImportResultResponse.ImportError err(int row, String field, String msg) {
        return ImportResultResponse.ImportError.builder()
                .row(row).field(field).message(msg).build();
    }

    private void parseDob(Row row, Map<String, Integer> colMap, String dobStr,
            UserProfileEntity up, List<ImportResultResponse.ImportError> errors, int rowNum) {
        Integer dobIdx = colMap.get("date_of_birth");
        if (dobIdx == null)
            return;
        Cell dobCell = row.getCell(dobIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (dobCell == null)
            return;
        if (dobCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dobCell)) {
            up.setDateOfBirth(dobCell.getDateCellValue()
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        } else if (!dobStr.isEmpty()) {
            try {
                up.setDateOfBirth(LocalDate.parse(dobStr));
            } catch (Exception e) {
                errors.add(err(rowNum, "date_of_birth", "Sai định dạng ngày (yyyy-MM-dd)"));
            }
        }
    }

    /**
     * Chuyển status từ Excel (English hoặc tiếng Việt) → đúng format trong DB.
     * DB dùng: "còn chỗ", "đã đầy"
     */
    private String normalizeRoomStatus(String raw) {
        if (raw == null || raw.isBlank())
            return "còn chỗ";
        return switch (raw.trim().toLowerCase()) {
            case "available", "con cho", "còn chỗ", "trống" -> "còn chỗ";
            case "full", "da day", "đã đầy", "day", "đầy" -> "đã đầy";
            default -> "còn chỗ"; // fallback an toàn
        };
    }

    // ─────────────────────────────────────────────────────────
    // IMPORT GRADES (lv2 + admin)
    // Required: student_code, course_code, semester_name, midterm_score,
    // final_score
    // Optional: assignment_score, total_score, letter_grade, grade_point, is_passed
    // ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ImportResultResponse importGrades(MultipartFile file, boolean overwrite) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        int success = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> colMap = buildColMap(sheet.getRow(0));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row))
                    continue;
                int rowNum = i + 1;
                try {
                    String studentCode = getCol(row, colMap, "student_code");
                    String courseCode = getCol(row, colMap, "course_code");
                    String semesterName = getCol(row, colMap, "semester_name");
                    String midtermStr = getCol(row, colMap, "midterm_score");
                    String finalStr = getCol(row, colMap, "final_score");

                    if (studentCode.isEmpty() || courseCode.isEmpty() || semesterName.isEmpty()) {
                        errors.add(err(rowNum, "required", "student_code, course_code, semester_name là bắt buộc"));
                        continue;
                    }

                    Optional<StudentProfileEntity> spOpt = studentProfileRepository.findByStudentCode(studentCode);
                    if (spOpt.isEmpty()) {
                        errors.add(err(rowNum, "student_code", "Không tìm thấy sinh viên: " + studentCode));
                        continue;
                    }
                    Long userId = spOpt.get().getUserId();

                    List<?> courseIds = em.createNativeQuery("SELECT course_id FROM course WHERE course_code=?")
                            .setParameter(1, courseCode).getResultList();
                    if (courseIds.isEmpty()) {
                        errors.add(err(rowNum, "course_code", "Không tìm thấy học phần: " + courseCode));
                        continue;
                    }
                    Long courseId = ((Number) courseIds.get(0)).longValue();

                    List<?> semIds = em
                            .createNativeQuery("SELECT semester_id FROM semester WHERE semester_name=? LIMIT 1")
                            .setParameter(1, semesterName).getResultList();
                    if (semIds.isEmpty()) {
                        errors.add(err(rowNum, "semester_name", "Không tìm thấy kỳ học: " + semesterName));
                        continue;
                    }
                    Long semId = ((Number) semIds.get(0)).longValue();

                    String assignmentStr = getCol(row, colMap, "assignment_score");
                    String totalStr = getCol(row, colMap, "total_score");
                    String letterGrade = getCol(row, colMap, "letter_grade");
                    String gradePointStr = getCol(row, colMap, "grade_point");
                    String isPassedStr = getCol(row, colMap, "is_passed");

                    int updated = em.createNativeQuery(
                            "UPDATE enrollment SET midterm_score=?, final_score=?, assignment_score=?, " +
                                    "total_score=?, letter_grade=?, grade_point=?, is_passed=? " +
                                    "WHERE user_id=? AND course_id=? AND semester_id=?")
                            .setParameter(1, midtermStr.isEmpty() ? null : Double.parseDouble(midtermStr))
                            .setParameter(2, finalStr.isEmpty() ? null : Double.parseDouble(finalStr))
                            .setParameter(3, assignmentStr.isEmpty() ? null : Double.parseDouble(assignmentStr))
                            .setParameter(4, totalStr.isEmpty() ? null : Double.parseDouble(totalStr))
                            .setParameter(5, letterGrade.isEmpty() ? null : letterGrade)
                            .setParameter(6, gradePointStr.isEmpty() ? null : Double.parseDouble(gradePointStr))
                            .setParameter(7, isPassedStr.isEmpty() ? null : Boolean.parseBoolean(isPassedStr))
                            .setParameter(8, userId).setParameter(9, courseId).setParameter(10, semId)
                            .executeUpdate();

                    if (updated == 0) {
                        errors.add(err(rowNum, "enrollment", "Sinh viên " + studentCode + " chưa đăng ký học phần "
                                + courseCode + " kỳ " + semesterName));
                    } else {
                        success++;
                    }
                } catch (NumberFormatException e) {
                    errors.add(err(rowNum, "format", "Điểm phải là số: " + e.getMessage()));
                } catch (Exception e) {
                    errors.add(err(rowNum, "unknown", e.getMessage()));
                }
            }
        } catch (Exception e) {
            errors.add(err(0, "file", "Lỗi đọc file: " + e.getMessage()));
        }
        return ImportResultResponse.builder().success(success).failed(errors.size()).errors(errors).build();
    }

    // ─────────────────────────────────────────────────────────
    // IMPORT SCHOLARSHIPS (lv3+ / advisor)
    // Required: student_code, scholarship_name
    // Optional: semester_id
    // ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ImportResultResponse importScholarships(MultipartFile file, boolean overwrite) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        int success = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> colMap = buildColMap(sheet.getRow(0));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row))
                    continue;
                int rowNum = i + 1;
                try {
                    String studentCode = getCol(row, colMap, "student_code");
                    String scholarshipName = getCol(row, colMap, "scholarship_name");

                    if (studentCode.isEmpty() || scholarshipName.isEmpty()) {
                        errors.add(err(rowNum, "required", "student_code, scholarship_name là bắt buộc"));
                        continue;
                    }

                    Optional<StudentProfileEntity> spOpt = studentProfileRepository.findByStudentCode(studentCode);
                    if (spOpt.isEmpty()) {
                        errors.add(err(rowNum, "student_code", "Không tìm thấy sinh viên: " + studentCode));
                        continue;
                    }
                    Long userId = spOpt.get().getUserId();

                    // Tìm hoặc tạo scholarship theo tên
                    List<?> schIds = em.createNativeQuery("SELECT scholarship_id FROM scholarship WHERE name=? LIMIT 1")
                            .setParameter(1, scholarshipName).getResultList();
                    Long scholarshipId;
                    if (schIds.isEmpty()) {
                        // Tạo mới scholarship record
                        em.createNativeQuery("INSERT INTO scholarship(name) VALUES(?)")
                                .setParameter(1, scholarshipName).executeUpdate();
                        scholarshipId = ((Number) em.createNativeQuery(
                                "SELECT scholarship_id FROM scholarship WHERE name=? LIMIT 1")
                                .setParameter(1, scholarshipName).getSingleResult()).longValue();
                    } else {
                        scholarshipId = ((Number) schIds.get(0)).longValue();
                    }

                    String semIdStr = getCol(row, colMap, "semester_id");
                    Long semId = semIdStr.isEmpty() ? null : Long.parseLong(semIdStr);

                    boolean exists = !em.createNativeQuery(
                            "SELECT 1 FROM student_scholarship WHERE user_id=? AND scholarship_id=?")
                            .setParameter(1, userId).setParameter(2, scholarshipId)
                            .getResultList().isEmpty();

                    if (exists && !overwrite) {
                        errors.add(err(rowNum, "duplicate",
                                "Sinh viên " + studentCode + " đã có học bổng này — dùng overwrite=true"));
                        continue;
                    }
                    if (exists) {
                        em.createNativeQuery(
                                "UPDATE student_scholarship SET pending_status='pending', semester_id=? WHERE user_id=? AND scholarship_id=?")
                                .setParameter(1, semId).setParameter(2, userId).setParameter(3, scholarshipId)
                                .executeUpdate();
                    } else {
                        em.createNativeQuery(
                                "INSERT INTO student_scholarship(user_id, scholarship_id, pending_status, semester_id) VALUES(?,?,'pending',?)")
                                .setParameter(1, userId).setParameter(2, scholarshipId).setParameter(3, semId)
                                .executeUpdate();
                    }
                    success++;
                } catch (Exception e) {
                    errors.add(err(rowNum, "unknown", e.getMessage()));
                }
            }
        } catch (Exception e) {
            errors.add(err(0, "file", "Lỗi đọc file: " + e.getMessage()));
        }
        return ImportResultResponse.builder().success(success).failed(errors.size()).errors(errors).build();
    }

    // ─────────────────────────────────────────────────────────
    // IMPORT WARNINGS (lv3+ / advisor)
    // Required: student_code, warning_type, semester_id
    // Optional: description
    // ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ImportResultResponse importWarnings(MultipartFile file, boolean overwrite) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        int success = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> colMap = buildColMap(sheet.getRow(0));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row))
                    continue;
                int rowNum = i + 1;
                try {
                    String studentCode = getCol(row, colMap, "student_code");
                    String warningType = getCol(row, colMap, "warning_type");
                    String semIdStr = getCol(row, colMap, "semester_id");

                    if (studentCode.isEmpty() || warningType.isEmpty() || semIdStr.isEmpty()) {
                        errors.add(err(rowNum, "required", "student_code, warning_type, semester_id là bắt buộc"));
                        continue;
                    }

                    Optional<StudentProfileEntity> spOpt = studentProfileRepository.findByStudentCode(studentCode);
                    if (spOpt.isEmpty()) {
                        errors.add(err(rowNum, "student_code", "Không tìm thấy sinh viên: " + studentCode));
                        continue;
                    }
                    Long userId = spOpt.get().getUserId();
                    Long semId = Long.parseLong(semIdStr);
                    String description = getCol(row, colMap, "description");

                    academicWarningRepository.save(AcademicWarningEntity.builder()
                            .userId(userId).semesterId(semId)
                            .warningType(warningType.toUpperCase())
                            .description(description.isEmpty() ? null : description)
                            .issuedAt(LocalDateTime.now())
                            .status("pending") // chờ lv5 duyệt
                            .build());
                    success++;
                } catch (NumberFormatException e) {
                    errors.add(err(rowNum, "format", "semester_id phải là số: " + e.getMessage()));
                } catch (Exception e) {
                    errors.add(err(rowNum, "unknown", e.getMessage()));
                }
            }
        } catch (Exception e) {
            errors.add(err(0, "file", "Lỗi đọc file: " + e.getMessage()));
        }
        return ImportResultResponse.builder().success(success).failed(errors.size()).errors(errors).build();
    }

}
package com.utc2.appreborn.backend.modules.dormitory.service.impl;

import com.utc2.appreborn.backend.exception.BadRequestException;
import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.auth.entity.User;
import com.utc2.appreborn.backend.modules.auth.repository.UserRepository;
import com.utc2.appreborn.backend.modules.dormitory.dto.DormRegisterRequest;
import com.utc2.appreborn.backend.modules.dormitory.dto.DormRegistrationDto;
import com.utc2.appreborn.backend.modules.dormitory.dto.DormRoomDto;
import com.utc2.appreborn.backend.modules.dormitory.entity.DormitoryRegistrationEntity;
import com.utc2.appreborn.backend.modules.dormitory.entity.DormitoryRoomEntity;
import com.utc2.appreborn.backend.modules.dormitory.repository.DormitoryRegistrationRepository;
import com.utc2.appreborn.backend.modules.dormitory.repository.DormitoryRoomRepository;
import com.utc2.appreborn.backend.modules.dormitory.service.DormitoryService;
import com.utc2.appreborn.backend.modules.finance.entity.TuitionFee;
import com.utc2.appreborn.backend.modules.finance.repository.TuitionFeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DormitoryServiceImpl implements DormitoryService {

    private static final String FEE_TYPE_DORMITORY = "DORMITORY";
    private static final String FEE_STATUS_UNPAID  = "chưa đóng";

    private final DormitoryRoomRepository         roomRepository;
    private final DormitoryRegistrationRepository registrationRepository;
    private final UserRepository                  userRepository;
    private final TuitionFeeRepository            tuitionFeeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Helper: lấy userId từ JWT — giống pattern trong ScheduleServiceImpl ──

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Long currentUserId() {
        return currentUser().getId();
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  1. DANH SÁCH PHÒNG KTX
    // ════════════════════════════════════════════════════════════════════════════

    @Override
    public List<DormRoomDto> getAllRooms() {
        return roomRepository.findAllRoomsOrdered()
                .stream()
                .map(this::mapToRoomDto)
                .toList();
    }

    private DormRoomDto mapToRoomDto(Object[] row) {
        // Column order từ DormitoryRoomRepository:
        // 0=room_id, 1=room_code, 2=building, 3=floor, 4=capacity,
        // 5=current_occupancy, 6=room_type, 7=price_per_month, 8=status, 9=amenities
        String status = (String) row[8];
        return DormRoomDto.builder()
                .roomId(((Number) row[0]).longValue())
                .roomCode((String) row[1])
                .building((String) row[2])
                .floor(row[3] != null ? ((Number) row[3]).intValue() : null)
                .capacity(row[4] != null ? ((Number) row[4]).intValue() : null)
                .currentOccupancy(row[5] != null ? ((Number) row[5]).intValue() : null)
                .roomType((String) row[6])
                .pricePerMonth(row[7] != null ? ((Number) row[7]).doubleValue() : null)
                .status(status)
                .amenities((String) row[9])
                .available("còn chỗ".equals(status))
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  2. LỊCH SỬ ĐĂNG KÝ CỦA SINH VIÊN ĐANG ĐĂNG NHẬP
    // ════════════════════════════════════════════════════════════════════════════

    @Override
    public List<DormRegistrationDto> getMyRegistrations() {
        Long userId = currentUserId();
        return registrationRepository.findRegistrationsByUserId(userId)
                .stream()
                .map(this::mapToRegistrationDto)
                .toList();
    }

    private DormRegistrationDto mapToRegistrationDto(Object[] row) {
        // Column order từ DormitoryRegistrationRepository:
        // 0=dorm_reg_id, 1=user_id, 2=room_id, 3=registered_at,
        // 4=start_date,  5=end_date, 6=status, 7=total_fee, 8=paid_status,
        // 9=room_code,   10=building, 11=room_type, 12=price_per_month
        return DormRegistrationDto.builder()
                .dormRegId(((Number) row[0]).longValue())
                .roomId(((Number) row[2]).longValue())
                .registeredAt(row[3] != null ? row[3].toString() : null)
                .startDate(formatDate(row[4]))
                .endDate(formatDate(row[5]))
                .status((String) row[6])
                .totalFee(row[7] != null ? ((Number) row[7]).doubleValue() : null)
                .paidStatus((String) row[8])
                .roomCode((String) row[9])
                .building((String) row[10])
                .roomType((String) row[11])
                .pricePerMonth(row[12] != null ? ((Number) row[12]).doubleValue() : null)
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  3. ĐĂNG KÝ PHÒNG KTX
    // ════════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public DormRegistrationDto register(DormRegisterRequest request) {
        User user = currentUser();
        Long userId = user.getId();

        DormitoryRoomEntity room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Phòng KTX không tồn tại"));

        if (!"còn chỗ".equals(room.getStatus())) {
            throw new BadRequestException("Phòng " + room.getRoomCode() + " hiện không còn chỗ!");
        }

        if (registrationRepository.existsByUserIdAndStatusNot(userId, "đã hủy")) {
            throw new BadRequestException("Bạn đã có đăng ký KTX đang hoạt động. Hủy trước khi đăng ký mới.");
        }

        LocalDate startDate = LocalDate.now();
        LocalDate endDate   = startDate.plusMonths(request.getMonths());
        Double    totalFee  = room.getPricePerMonth() * request.getMonths();

        DormitoryRegistrationEntity saved = registrationRepository.save(
                DormitoryRegistrationEntity.builder()
                        .userId(userId)
                        .roomId(room.getRoomId())
                        .startDate(startDate)
                        .endDate(endDate)
                        .status("chờ duyệt")
                        .totalFee(totalFee)
                        .paidStatus("chưa đóng")
                        .build()
        );

        // Cập nhật current_occupancy và status phòng
        room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
        if (room.getCurrentOccupancy() >= room.getCapacity()) {
            room.setStatus("đã đầy");
        }
        roomRepository.save(room);

        // ── Tạo fee record cho KTX ────────────────────────────────────────────
        BigDecimal dormTotal = BigDecimal.valueOf(totalFee);
        tuitionFeeRepository.findByDormRegId(saved.getDormRegId())
                .ifPresentOrElse(
                        fee -> { /* đã có fee — không tạo lại */ },
                        () -> tuitionFeeRepository.save(TuitionFee.builder()
                                .user(user)
                                .dormRegId(saved.getDormRegId())
                                .feeType(FEE_TYPE_DORMITORY)
                                .totalAmount(dormTotal)
                                .paidAmount(BigDecimal.ZERO)
                                .remainingAmount(dormTotal)
                                .dueDate(endDate)
                                .status(FEE_STATUS_UNPAID)
                                .build())
                );

        return DormRegistrationDto.builder()
                .dormRegId(saved.getDormRegId())
                .roomId(room.getRoomId())
                .roomCode(room.getRoomCode())
                .building(room.getBuilding())
                .roomType(room.getRoomType())
                .pricePerMonth(room.getPricePerMonth())
                .startDate(startDate.format(DATE_FMT))
                .endDate(endDate.format(DATE_FMT))
                .status(saved.getStatus())
                .totalFee(saved.getTotalFee())
                .paidStatus(saved.getPaidStatus())
                .registeredAt(saved.getRegisteredAt() != null ? saved.getRegisteredAt().toString() : null)
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  4. HỦY ĐĂNG KÝ KTX
    // ════════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void cancelRegistration(Long dormRegId) {
        Long userId = currentUserId();

        DormitoryRegistrationEntity reg = registrationRepository.findById(dormRegId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký KTX"));

        if (!reg.getUserId().equals(userId)) {
            throw new BadRequestException("Không có quyền hủy đăng ký này");
        }

        if ("đã hủy".equals(reg.getStatus())) {
            throw new BadRequestException("Đăng ký này đã bị hủy rồi");
        }

        reg.setStatus("đã hủy");
        registrationRepository.save(reg);

        // ── Xóa fee KTX nếu chưa đóng tiền ────────────────────────────────
        tuitionFeeRepository.deleteByDormRegIdAndStatus(reg.getDormRegId(), FEE_STATUS_UNPAID);

        // Giảm current_occupancy và mở lại phòng nếu trước đó đã đầy
        roomRepository.findById(reg.getRoomId()).ifPresent(room -> {
            if (room.getCurrentOccupancy() > 0) {
                room.setCurrentOccupancy(room.getCurrentOccupancy() - 1);
            }
            if ("đã đầy".equals(room.getStatus()) && room.getCurrentOccupancy() < room.getCapacity()) {
                room.setStatus("còn chỗ");
            }
            roomRepository.save(room);
        });
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  5. THANH TOÁN PHÍ KTX
    // ════════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public DormRegistrationDto payDorm(Long dormRegId) {
        Long userId = currentUserId();

        DormitoryRegistrationEntity reg = registrationRepository.findById(dormRegId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký KTX"));

        // Chỉ sinh viên sở hữu đăng ký mới được thanh toán
        if (!reg.getUserId().equals(userId)) {
            throw new BadRequestException("Không có quyền thanh toán đăng ký này");
        }

        // Kiểm tra đã đóng chưa
        if ("đã đóng".equals(reg.getPaidStatus())) {
            throw new BadRequestException("Phí KTX này đã được đóng rồi");
        }

        // Chỉ đăng ký đã duyệt mới được đóng tiền
        if (!"đã duyệt".equals(reg.getStatus())) {
            throw new BadRequestException("Đăng ký KTX chưa được duyệt, chưa thể thanh toán");
        }

        reg.setPaidStatus("đã đóng");
        registrationRepository.save(reg);

        // ── Cập nhật fee record tương ứng ────────────────────────────────────
        tuitionFeeRepository.findByDormRegId(dormRegId).ifPresent(fee -> {
            if (!"đã đóng đủ".equals(fee.getStatus())) {
                fee.setPaidAmount(fee.getTotalAmount());
                fee.setRemainingAmount(java.math.BigDecimal.ZERO);
                fee.setStatus("đã đóng đủ");
                fee.setPaymentMethod("online");
                fee.setPaidAt(java.time.LocalDateTime.now());
                tuitionFeeRepository.save(fee);
            }
        });

        // Map lại DTO để trả về — lấy thêm info phòng
        DormitoryRoomEntity room = roomRepository.findById(reg.getRoomId()).orElse(null);
        return DormRegistrationDto.builder()
                .dormRegId(reg.getDormRegId())
                .roomId(reg.getRoomId())
                .roomCode(room != null ? room.getRoomCode() : null)
                .building(room != null ? room.getBuilding() : null)
                .roomType(room != null ? room.getRoomType() : null)
                .pricePerMonth(room != null ? room.getPricePerMonth() : null)
                .startDate(reg.getStartDate() != null ? reg.getStartDate().format(DATE_FMT) : null)
                .endDate(reg.getEndDate() != null ? reg.getEndDate().format(DATE_FMT) : null)
                .status(reg.getStatus())
                .totalFee(reg.getTotalFee())
                .paidStatus(reg.getPaidStatus())
                .registeredAt(reg.getRegisteredAt() != null ? reg.getRegisteredAt().toString() : null)
                .build();
    }

    private String formatDate(Object o) {
        if (o == null) return null;
        try {
            if (o instanceof java.sql.Date sqlDate) {
                return sqlDate.toLocalDate().format(DATE_FMT);
            }
            return o.toString();
        } catch (Exception e) {
            return null;
        }
    }
}

package com.utc2.appreborn.backend.modules.dormitory.service;

import com.utc2.appreborn.backend.modules.dormitory.dto.DormRegisterRequest;
import com.utc2.appreborn.backend.modules.dormitory.dto.DormRegistrationDto;
import com.utc2.appreborn.backend.modules.dormitory.dto.DormRoomDto;

import java.util.List;

public interface DormitoryService {

    List<DormRoomDto> getAllRooms();

    List<DormRegistrationDto> getMyRegistrations();

    DormRegistrationDto register(DormRegisterRequest request);

    void cancelRegistration(Long dormRegId);

    /** Thanh toán toàn bộ phí KTX của 1 đăng ký — chỉ đóng 1 lần đủ */
    DormRegistrationDto payDorm(Long dormRegId);
}
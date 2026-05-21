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
}

package com.utc2.appreborn.backend.modules.public_services.repository;

import com.utc2.appreborn.backend.modules.public_services.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByUserIdOrderBySubmittedAtDesc(Long userId);
    List<ServiceRequest> findByUserIdAndServiceTypeOrderBySubmittedAtDesc(Long userId, String serviceType);
}
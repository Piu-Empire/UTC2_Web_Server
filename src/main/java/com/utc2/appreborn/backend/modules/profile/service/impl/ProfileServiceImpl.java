package com.utc2.appreborn.backend.modules.profile.service.impl;

import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.auth.entity.User;
import com.utc2.appreborn.backend.modules.auth.repository.UserRepository;
import com.utc2.appreborn.backend.modules.profile.dto.ProfileResponse;
import com.utc2.appreborn.backend.modules.profile.service.ProfileService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager em;

    @Override
    public ProfileResponse getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        Long userId = user.getId();

        // Lấy user_profile
        Object[] up = queryOne(
                "SELECT full_name, phone_number, avatar_url, date_of_birth, gender " +
                "FROM user_profile WHERE user_id = :id", userId);

        // Lấy student_profile
        Object[] sp = queryOne(
                "SELECT student_code, faculty, major, academic_year, class_name, status " +
                "FROM student_profile WHERE user_id = :id", userId);

        return ProfileResponse.builder()
                .id(userId)
                .email(user.getEmail())
                // user_profile
                .fullName(      up != null ? str(up[0]) : null)
                .phoneNumber(   up != null ? str(up[1]) : null)
                .avatarUrl(     up != null ? str(up[2]) : null)
                .dateOfBirth(   up != null && up[3] != null
                                ? ((java.sql.Date) up[3]).toLocalDate() : null)
                .gender(        up != null ? str(up[4]) : null)
                // student_profile
                .studentId(     sp != null ? str(sp[0]) : null)
                .faculty(       sp != null ? str(sp[1]) : null)
                .major(         sp != null ? str(sp[2]) : null)
                .academicYear(  sp != null ? str(sp[3]) : null)
                .className(     sp != null ? str(sp[4]) : null)
                .status(        sp != null ? str(sp[5]) : null)
                .build();
    }

    private Object[] queryOne(String sql, Long id) {
        try {
            return (Object[]) em.createNativeQuery(sql)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    private String str(Object o) {
        return o != null ? o.toString() : null;
    }
}

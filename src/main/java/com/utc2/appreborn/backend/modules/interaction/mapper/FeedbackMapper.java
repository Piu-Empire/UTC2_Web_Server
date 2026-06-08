package com.utc2.appreborn.backend.modules.interaction.mapper;

import com.utc2.appreborn.backend.modules.interaction.dto.FeedbackResponse;
import com.utc2.appreborn.backend.modules.interaction.entity.FeedbackEntity;
import com.utc2.appreborn.backend.modules.profile.entity.StudentProfileEntity;
import com.utc2.appreborn.backend.modules.profile.entity.UserProfileEntity;
import com.utc2.appreborn.backend.modules.profile.repository.StudentProfileRepository;
import com.utc2.appreborn.backend.modules.profile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedbackMapper {

    private final UserProfileRepository    userProfileRepository;
    private final StudentProfileRepository studentProfileRepository;

    public FeedbackResponse toResponse(FeedbackEntity e) {
        Long userId = e.getUser().getId();

        // Lấy fullName từ user_profile
        String fullName = userProfileRepository.findById(userId)
                .map(UserProfileEntity::getFullName)
                .orElse(null);

        // Lấy studentCode từ student_profile
        String studentCode = studentProfileRepository.findById(userId)
                .map(StudentProfileEntity::getStudentCode)
                .orElse(null);

        return FeedbackResponse.builder()
                .id(e.getId())
                .type(e.getType())
                .content(e.getContent())
                .status(e.getStatus())
                .adminReply(e.getAdminReply())
                .submittedAt(e.getSubmittedAt())
                .studentName(fullName)
                .studentCode(studentCode)
                .build();
    }
}
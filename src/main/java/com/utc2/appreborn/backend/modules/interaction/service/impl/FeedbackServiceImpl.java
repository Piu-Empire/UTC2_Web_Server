package com.utc2.appreborn.backend.modules.interaction.service.impl;

import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.auth.entity.User;
import com.utc2.appreborn.backend.modules.interaction.dto.FeedbackRequest;
import com.utc2.appreborn.backend.modules.interaction.dto.FeedbackResponse;
import com.utc2.appreborn.backend.modules.interaction.entity.FeedbackEntity;
import com.utc2.appreborn.backend.modules.interaction.mapper.FeedbackMapper;
import com.utc2.appreborn.backend.modules.interaction.repository.FeedbackRepository;
import com.utc2.appreborn.backend.modules.interaction.service.FeedbackService;
import com.utc2.appreborn.backend.modules.profile.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository       feedbackRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FeedbackMapper           feedbackMapper;

    // ── Student ──────────────────────────────────────────────

    @Override
    @Transactional
    public FeedbackResponse submit(String username, FeedbackRequest request) {
        User user = findUser(username);
        FeedbackEntity entity = FeedbackEntity.builder()
                .user(user)
                .type(request.getType())
                .content(request.getContent())
                .build();
        return feedbackMapper.toResponse(feedbackRepository.save(entity));
    }

    @Override
    public List<FeedbackResponse> getMyFeedbacks(String username) {
        User user = findUser(username);
        return feedbackRepository
                .findByUserIdOrderBySubmittedAtDesc(user.getId())
                .stream().map(feedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ── Admin ────────────────────────────────────────────────

    @Override
    public List<FeedbackResponse> getAll(String status) {
        List<FeedbackEntity> list = (status != null && !status.isBlank())
                ? feedbackRepository.findByStatusOrderBySubmittedAtDesc(status)
                : feedbackRepository.findAllByOrderBySubmittedAtDesc();
        return list.stream().map(feedbackMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FeedbackResponse reply(Long id, String adminReply) {
        FeedbackEntity entity = findById(id);
        entity.setAdminReply(adminReply);
        entity.setStatus("đã phản hồi");
        return feedbackMapper.toResponse(feedbackRepository.save(entity));
    }

    @Override
    @Transactional
    public FeedbackResponse updateStatus(Long id, String status) {
        FeedbackEntity entity = findById(id);
        entity.setStatus(status);
        return feedbackMapper.toResponse(feedbackRepository.save(entity));
    }

    // ── Helpers ──────────────────────────────────────────────

    private User findUser(String username) {
        String studentCode = username.contains("@") ? username.split("@")[0] : username;
        return studentProfileRepository.findByStudentCodeWithUser(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sinh viên: " + studentCode))
                .getUser();
    }

    private FeedbackEntity findById(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy feedback id: " + id));
    }
}
package com.utc2.appreborn.backend.modules.interaction.service;

import com.utc2.appreborn.backend.modules.interaction.dto.FeedbackRequest;
import com.utc2.appreborn.backend.modules.interaction.dto.FeedbackResponse;
import java.util.List;

public interface FeedbackService {

    /** Sinh viên gửi phản hồi */
    FeedbackResponse submit(String username, FeedbackRequest request);

    /** Lịch sử phản hồi của sinh viên */
    List<FeedbackResponse> getMyFeedbacks(String username);

    /** Admin: lấy tất cả, lọc theo status */
    List<FeedbackResponse> getAll(String status);

    /** Admin: gửi phản hồi cho sinh viên */
    FeedbackResponse reply(Long id, String adminReply);

    /** Admin: cập nhật status */
    FeedbackResponse updateStatus(Long id, String status);
}
package com.utc2.appreborn.backend.modules.finance.service;

import com.utc2.appreborn.backend.modules.finance.dto.TuitionResponse;
import com.utc2.appreborn.backend.modules.finance.dto.TuitionSummaryResponse;

import java.util.List;

public interface TuitionService {
    TuitionSummaryResponse  getMyTuitionSummary(String username);
    List<TuitionResponse>   getMyTuitionHistory(String username);
    TuitionResponse         getTuitionBySemester(String username, String semester);
    /** Thanh toán toàn bộ học phí còn lại của 1 kỳ */
    TuitionResponse         payTuition(String username, Long semesterId, String paymentMethod);
    /** Lịch sử các kỳ đã đóng đủ — dùng cho Invoice screen */
    List<TuitionResponse>   getMyPaidHistory(String username);
}
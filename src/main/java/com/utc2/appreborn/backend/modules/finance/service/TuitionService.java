package com.utc2.appreborn.backend.modules.finance.service;

import com.utc2.appreborn.backend.modules.finance.dto.TuitionResponse;
import com.utc2.appreborn.backend.modules.finance.dto.TuitionSummaryResponse;

import java.util.List;

public interface TuitionService {
    TuitionSummaryResponse getMyTuitionSummary(String username);
    List<TuitionResponse> getMyTuitionHistory(String username);
    TuitionResponse getTuitionBySemester(String username, String semester);
}
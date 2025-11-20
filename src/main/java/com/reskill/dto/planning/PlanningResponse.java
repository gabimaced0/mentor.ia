package com.reskill.dto.planning;

import com.reskill.dto.planningItem.PlanningItemResponse;

import java.time.LocalDateTime;
import java.util.List;

public record PlanningResponse(
        Long id,
        String goal,
        String recommendation,
        LocalDateTime createdAt,
        Long userId,
        List<PlanningItemResponse> items
) {
}

package com.reskill.dto.planningItem;

public record PlanningItemResponse(
        Long id,
        String description,
        boolean completed
) {
}

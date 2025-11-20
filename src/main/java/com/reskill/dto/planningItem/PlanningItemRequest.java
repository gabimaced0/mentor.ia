package com.reskill.dto.planningItem;

import jakarta.validation.constraints.NotBlank;

public record PlanningItemRequest(
        @NotBlank(message = "{planningitem.description.notblank}")
        String description
) {
}

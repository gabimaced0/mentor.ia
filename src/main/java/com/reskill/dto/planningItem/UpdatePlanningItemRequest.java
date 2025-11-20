package com.reskill.dto.planningItem;

import jakarta.validation.constraints.NotBlank;

public record UpdatePlanningItemRequest(
        @NotBlank(message = "{planningitem.description.notblank}")
        String description,
        boolean completed
) {
}

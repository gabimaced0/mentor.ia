package com.reskill.dto.planning;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreatePlanningRequest(
        @NotBlank(message = "{planning.goal.notblank}")
        String goal,
        @NotNull(message = "{planning.skills.notnull}")
        List<String> skills
) {
}

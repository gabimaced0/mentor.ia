package com.reskill.dto.skill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SkillRequest(
        @NotBlank(message = "{skill.name.notblank}")
        @Size(max = 100, message = "{skill.name.size}") String name
) {
}

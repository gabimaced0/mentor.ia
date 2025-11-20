package com.reskill.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(@NotBlank(message = "{user.nome.notblank}") String name,
                                @Email(message = "{user.email.invalid}") @NotBlank(message = "{user.email.notblank}") String email) {
}

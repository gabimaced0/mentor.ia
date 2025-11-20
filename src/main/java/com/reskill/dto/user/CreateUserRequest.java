package com.reskill.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(@NotBlank(message = "{user.name.notblank}") String name,
                                @Email(message = "{user.email.invalid}") @NotBlank(message = "{user.email.notblank}") String email,
                                @NotBlank(message = "{user.password.notblank}")  @Pattern(
                                        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+=\\-{}\\[\\]|:;\"'<>,.?/]).{8,24}$",
                                        message = "{user.password.pattern}"
                                ) String password) {
}

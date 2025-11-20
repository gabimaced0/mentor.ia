package com.reskill.dto.auth;

public record TokenResponse(String token, String email, Long expiresInMinutes) {
}

package com.reskill.dto;

public record EmailMessage(
        String to,
        String subject,
        String body
) {
}

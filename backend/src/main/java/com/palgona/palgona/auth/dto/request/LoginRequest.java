package com.palgona.palgona.auth.dto.request;

public record LoginRequest(
        String email,
        String password
) {
}

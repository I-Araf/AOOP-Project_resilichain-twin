package com.resilichain.api.api.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        UserResponse user
) {
}

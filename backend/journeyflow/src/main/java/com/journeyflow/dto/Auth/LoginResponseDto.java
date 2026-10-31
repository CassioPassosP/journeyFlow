package com.journeyflow.dto.Auth;

public record LoginResponseDto(
        String token,
        String tokenType,
        String userId,
        String email,
        String role
) {}
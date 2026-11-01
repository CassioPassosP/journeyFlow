package com.journeyflow.dto.Manager;

import java.time.OffsetDateTime;

import com.journeyflow.models.ManagerEntity;

public record ManagerResponseDto(
        Long id,
        Long storeId,
        String name,
        String email,
        String position,
        OffsetDateTime createdAt
) {
    public static ManagerResponseDto from(ManagerEntity manager) {
        return new ManagerResponseDto(
                manager.getId(),
                manager.getStore().getId(),
                manager.getName(),
                manager.getEmail(),
                manager.getPosition(),
                manager.getCreatedAt());
    }
}
package com.journeyflow.dto.Manager;

import com.journeyflow.models.ManagerEntity;
import com.journeyflow.models.StoreEntity;

import java.time.OffsetDateTime;

public record ManagerResponseDto(
        Long id,
        StoreEntity store,
        String name,
        String email,
        String password,
        String position,
        OffsetDateTime createdAt
) {
    public ManagerResponseDto(ManagerEntity manager) {
        this(
                manager.getId(),
                manager.getStore(),
                manager.getName(),
                manager.getEmail(),
                manager.getPassword(),
                manager.getPosition(),
                manager.getCreatedAt()
        );
    }
}

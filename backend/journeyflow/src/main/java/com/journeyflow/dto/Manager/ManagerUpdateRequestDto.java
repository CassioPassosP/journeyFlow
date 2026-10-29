package com.journeyflow.dto.Manager;

import com.journeyflow.models.StoreEntity;

import java.util.UUID;

public record ManagerUpdateRequestDto(
        UUID id,
        StoreEntity store,
        String name,
        String email,
        String password,
        String position
) {}

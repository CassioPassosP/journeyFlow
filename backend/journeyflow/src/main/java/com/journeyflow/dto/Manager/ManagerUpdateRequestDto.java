package com.journeyflow.dto.Manager;

import com.journeyflow.models.StoreEntity;

public record ManagerUpdateRequestDto(
        Long id,
        StoreEntity store,
        String name,
        String email,
        String password,
        String position
) {}
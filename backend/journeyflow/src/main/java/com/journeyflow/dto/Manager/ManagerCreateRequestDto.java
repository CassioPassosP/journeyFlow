package com.journeyflow.dto.Manager;

import com.journeyflow.models.StoreEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ManagerCreateRequestDto(
        StoreEntity store,
        String name,
        String email,
        String password,
        String position
) {}

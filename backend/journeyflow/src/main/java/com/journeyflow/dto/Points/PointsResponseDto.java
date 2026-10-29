package com.journeyflow.dto.Points;

import com.journeyflow.models.EmployeeEntity;

import java.time.OffsetDateTime;

public record PointsResponseDto(
        Long id,
        EmployeeEntity employee,
        String reason,
        Integer points,
        OffsetDateTime createdAt
) {}

package com.journeyflow.dto.Points;

import com.journeyflow.models.EmployeeEntity;

import java.time.OffsetDateTime;

public record PointsUpdateRequestDto(
        Long id,
        EmployeeEntity employee,
        Integer points
        )
{}

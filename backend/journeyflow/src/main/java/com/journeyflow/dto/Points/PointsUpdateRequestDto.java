package com.journeyflow.dto.Points;

import com.journeyflow.models.EmployeeEntity;

public record PointsUpdateRequestDto(
        Long id,
        EmployeeEntity employee,
        Integer points
) {}
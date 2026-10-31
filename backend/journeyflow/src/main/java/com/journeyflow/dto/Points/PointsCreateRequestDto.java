package com.journeyflow.dto.Points;

import com.journeyflow.models.EmployeeEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PointsCreateRequestDto(
        @NotNull EmployeeEntity employee,
        @NotBlank String reason,
        @NotNull Integer points
) {}
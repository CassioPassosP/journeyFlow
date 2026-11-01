package com.journeyflow.dto.Points;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PointsCreateRequestDto(
        @NotNull UUID employeeId,
        @NotBlank @Size(max = 200) String reason,
        @NotNull Integer points
) {}
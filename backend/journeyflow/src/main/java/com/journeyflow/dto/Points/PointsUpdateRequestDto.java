package com.journeyflow.dto.Points;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PointsUpdateRequestDto(
        @NotBlank @Size(max = 200) String reason,
        @NotNull Integer points
) {}
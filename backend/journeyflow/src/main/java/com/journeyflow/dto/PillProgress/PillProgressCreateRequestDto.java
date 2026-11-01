package com.journeyflow.dto.PillProgress;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record PillProgressCreateRequestDto(
        @NotNull UUID employeeId,
        @NotNull Long pillId
) {}
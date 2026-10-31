package com.journeyflow.dto.PillProgress;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PillProgressUpdateRequestDto(
        @NotNull UUID employeeId,
        @NotNull Long pillId
) {}
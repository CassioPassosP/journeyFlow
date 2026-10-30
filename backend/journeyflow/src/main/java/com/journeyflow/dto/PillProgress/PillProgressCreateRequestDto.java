package com.journeyflow.dto.PillProgress;

import java.util.UUID;

public record PillProgressCreateRequestDto(
        UUID employeeId,
        Long pillId
) {}
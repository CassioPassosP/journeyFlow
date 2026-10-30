package com.journeyflow.dto.PillProgress;

import java.util.UUID;

public record PillProgressUpdateRequestDto(
        UUID employeeId,
        Long pillId
) {}
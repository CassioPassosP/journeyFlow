package com.journeyflow.dto.PillProgress;

import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.PillsEntity;

import java.time.OffsetDateTime;

public record PillProgressCreateRequestDto(
        EmployeeEntity employee,
        PillsEntity pill,
        OffsetDateTime completedAt
) {}

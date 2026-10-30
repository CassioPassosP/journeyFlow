package com.journeyflow.dto.PillProgress;

import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.PillProgressEntity;
import com.journeyflow.models.PillsEntity;

import java.time.OffsetDateTime;

public record PillProgressResponseDto(
        Long id,
        EmployeeEntity employee,
        PillsEntity pill,
        OffsetDateTime completedAt
) {
    public PillProgressResponseDto(PillProgressEntity progress) {
        this(
                null,
                progress.getEmployee(),
                progress.getPill(),
                progress.getCompletedAt()
        );
    }
}

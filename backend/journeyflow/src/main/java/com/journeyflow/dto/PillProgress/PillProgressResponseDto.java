package com.journeyflow.dto.PillProgress;

import com.journeyflow.models.PillProgressEntity;
import com.journeyflow.models.PillProgressId;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PillProgressResponseDto(
        PillProgressId id,
        UUID employeeId,
        Long pillId,
        OffsetDateTime completedAt
) {
    public PillProgressResponseDto(PillProgressEntity progress) {
        this(
                progress.getId(),
                progress.getEmployee().getId(),
                progress.getPill().getId(),
                progress.getCompletedAt()
        );
    }
}
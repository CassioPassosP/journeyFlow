package com.journeyflow.dto.PillProgress;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.journeyflow.models.PillProgressEntity;

public record PillProgressResponseDto(
        UUID employeeId,
        Long pillId,
        OffsetDateTime completedAt
) {
    public static PillProgressResponseDto from(PillProgressEntity p) {
        return new PillProgressResponseDto(p.getId().getUserId(), p.getId().getPillId(), p.getCompletedAt());
    }
}
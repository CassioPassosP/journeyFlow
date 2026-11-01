package com.journeyflow.dto.Points;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.journeyflow.models.PointsEntity;

public record PointsResponseDto(
        Long id,
        UUID employeeId,
        String reason,
        Integer points,
        OffsetDateTime createdAt
) {
    public static PointsResponseDto from(PointsEntity p) {
        return new PointsResponseDto(
                p.getId(),
                p.getEmployee() == null ? null : p.getEmployee().getId(),
                p.getReason(),
                p.getPoints(),
                p.getCreatedAt());
    }
}
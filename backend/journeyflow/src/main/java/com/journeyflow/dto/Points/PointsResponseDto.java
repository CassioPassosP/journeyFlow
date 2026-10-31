package com.journeyflow.dto.Points;

import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.PointsEntity;

import java.time.OffsetDateTime;

public record PointsResponseDto(
        Long id,
        EmployeeEntity employee,
        String reason,
        Integer points,
        OffsetDateTime createdAt
) {
    public PointsResponseDto(PointsEntity pointsEntity) {
        this(
                pointsEntity.getId(),
                pointsEntity.getEmployee(),
                pointsEntity.getReason(),
                pointsEntity.getPoints(),
                pointsEntity.getCreatedAt()
        );
    }
}

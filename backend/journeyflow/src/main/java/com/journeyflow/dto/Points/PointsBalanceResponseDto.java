package com.journeyflow.dto.Points;

import java.util.UUID;

public record PointsBalanceResponseDto(UUID employeeId, long balance) {}

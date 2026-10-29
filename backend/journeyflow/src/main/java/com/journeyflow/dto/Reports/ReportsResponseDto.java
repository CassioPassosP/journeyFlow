package com.journeyflow.dto.Reports;

import com.journeyflow.models.EmployeeEntity;

import java.time.OffsetDateTime;

public record ReportsResponseDto(
        Long id,
        EmployeeEntity employee,
        String originalText,
        String editedText,
        String summary,
        String topic,
        String kind,
        String source,
        String status,
        boolean helpRequest,
        OffsetDateTime createdAt
) {}

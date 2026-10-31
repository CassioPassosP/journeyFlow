package com.journeyflow.dto.Reports;

import com.journeyflow.models.ReportsEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportsResponseDto(
        Long id,
        UUID employeeId,
        String originalText,
        String editedText,
        String summary,
        String topic,
        String kind,
        String source,
        String status,
        boolean helpRequest,
        OffsetDateTime createdAt
) {
    public ReportsResponseDto(ReportsEntity report) {
        this(
                report.getId(),
                report.getEmployee().getId(),
                report.getOriginalText(),
                report.getEditedText(),
                report.getSummary(),
                report.getTopic(),
                report.getKind() != null ? report.getKind().name() : null,
                report.getSource().name(),
                report.getStatus().name(),
                report.isHelpRequest(),
                report.getCreatedAt()
        );
    }
}

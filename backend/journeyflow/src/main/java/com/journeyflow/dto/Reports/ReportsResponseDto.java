package com.journeyflow.dto.Reports;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.journeyflow.models.ReportsEntity;
import com.journeyflow.models.enums.ContentSource;
import com.journeyflow.models.enums.ReportKind;
import com.journeyflow.models.enums.ReportStatus;

public record ReportsResponseDto(
        Long id,
        UUID employeeId,
        String originalText,
        String editedText,
        String summary,
        String topic,
        ReportKind kind,
        ContentSource source,
        ReportStatus status,
        boolean helpRequest,
        OffsetDateTime createdAt
) {
    public static ReportsResponseDto from(ReportsEntity r) {
        return new ReportsResponseDto(
                r.getId(),
                r.getEmployeeId(),
                r.getOriginalText(),
                r.getEditedText(),
                r.getSummary(),
                r.getTopic(),
                r.getKind(),
                r.getSource(),
                r.getStatus(),
                r.isHelpRequest(),
                r.getCreatedAt());
    }
}
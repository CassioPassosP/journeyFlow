package com.journeyflow.dto.Alert;

import java.sql.Date;
import java.time.OffsetDateTime;

import com.journeyflow.models.AlertEntity;

public record AlertResponseDto(
        Long id,
        Long report_id,
        Long assigned_to,
        String destination,
        String suggestion,
        String urgency,
        String status,
        boolean reminder_sent,
        OffsetDateTime created_at,
        OffsetDateTime answered_at,
        Date alert_date,
        String alert_title,
        boolean confirmed,
        String subtitle
) {
    public AlertResponseDto(AlertEntity alert) {
        this(
                alert.getId(),
                alert.getReport().getId(),
                alert.getAssignedTo() != null ? alert.getAssignedTo().getId() : null,
                alert.getDestination().name(),
                alert.getSuggestion(),
                alert.getUrgency().name(),
                alert.getStatus().name(),
                alert.isReminderSent(),
                alert.getCreatedAt(),
                alert.getAnsweredAt(),
                Date.valueOf(alert.getAlertDate().toLocalDate()),
                alert.getAlertTitle(),
                alert.isConfirmed(),
                alert.getSubtitle()
        );
    }
}

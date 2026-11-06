package com.journeyflow.dto.Alert;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.journeyflow.models.AlertEntity;
import com.journeyflow.models.enums.AlertDestination;
import com.journeyflow.models.enums.AlertStatus;
import com.journeyflow.models.enums.AlertUrgency;

public record AlertResponseDto(
        Long id,
        Long reportId,
        UUID employeeId,
        Long storeId,
        Long assignedManagerId,
        AlertDestination destination,
        String suggestion,
        AlertUrgency urgency,
        AlertStatus status,
        boolean reminderSent,
        OffsetDateTime createdAt,
        OffsetDateTime answeredAt,
        LocalDateTime alertDate,
        String alertTitle,
        boolean confirmed,
        String subtitle
) {
    public static AlertResponseDto from(AlertEntity a) {
        return new AlertResponseDto(
                a.getId(),
                a.getReport() != null ? a.getReport().getId() : null,
                a.getEmployeeId(),
                a.getStore().getId(),
                a.getAssignedTo() != null ? a.getAssignedTo().getId() : null,
                a.getDestination(),
                a.getSuggestion(),
                a.getUrgency(),
                a.getStatus(),
                a.isReminderSent(),
                a.getCreatedAt(),
                a.getAnsweredAt(),
                a.getAlertDate(),
                a.getAlertTitle(),
                a.isConfirmed(),
                a.getSubtitle());
    }
}
package com.journeyflow.dto.Alert;

import com.journeyflow.models.enums.AlertDestination;
import com.journeyflow.models.enums.AlertStatus;
import com.journeyflow.models.enums.AlertUrgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.sql.Date;
import java.time.OffsetDateTime;

public record AlertCreateRequestDto(
        @NotNull Long report_id,
        Long assigned_to,
        @NotNull Long store_id,
        @NotNull AlertDestination destination,
        String suggestion,
        @NotNull AlertUrgency urgency,
        AlertStatus status,
        boolean reminder_sent,
        OffsetDateTime answered_at,
        @NotNull Date alert_date,
        @NotBlank String alert_title,
        boolean confirmed,
        @NotBlank String subtitle
) {}
package com.journeyflow.dto.Alert;

import com.journeyflow.models.enums.AlertDestination;
import com.journeyflow.models.enums.AlertStatus;
import com.journeyflow.models.enums.AlertUrgency;

import java.sql.Date;
import java.time.OffsetDateTime;

public record AlertCreateRequestDto(
        Long report_id,
        Long assigned_to,
        Long store_id,
        AlertDestination destination,
        String suggestion,
        AlertUrgency urgency,
        AlertStatus status,
        boolean reminder_sent,
        OffsetDateTime answered_at,
        Date alert_date,
        String alert_title,
        boolean confirmed,
        String subtitle
) {}
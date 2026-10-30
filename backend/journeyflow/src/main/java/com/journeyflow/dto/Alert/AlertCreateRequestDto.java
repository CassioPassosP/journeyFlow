package com.journeyflow.dto.Alert;

import java.sql.Date;
import java.time.OffsetDateTime;

public record AlertCreateRequestDto(
        Long report_id,
        Long assigned_to,
        String destination,
        String suggestion,
        String urgency,
        String status,
        boolean reminder_sent,
        OffsetDateTime answered_at,
        Date alert_date,
        String alert_title,
        boolean confirmed,
        String subtitle
)
{}

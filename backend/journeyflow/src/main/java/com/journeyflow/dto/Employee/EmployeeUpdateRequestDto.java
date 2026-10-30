package com.journeyflow.dto.Employee;

import com.journeyflow.models.ChatEntity;
import com.journeyflow.models.PillsEntity;
import com.journeyflow.models.PointsEntity;
import com.journeyflow.models.StoreEntity;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

public record EmployeeUpdateRequestDto(
        UUID id,
        StoreEntity store,
        String name,
        String email,
        String password,
        String position,
        PointsEntity points,
        ChatEntity messages,
        PillsEntity pilss,
        Date initiationDate,
        String currentState,
        boolean inLearningPath,
        OffsetDateTime lastInteraction,
        OffsetDateTime registerDate,
        Date startDate,
        boolean consent,
        OffsetDateTime consentAt,
        int streak,
        Date lastActive
)
{}

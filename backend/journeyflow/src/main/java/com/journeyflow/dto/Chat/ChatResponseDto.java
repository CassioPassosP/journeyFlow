package com.journeyflow.dto.Chat;

import java.time.Instant;
import java.time.OffsetDateTime;

public record ChatResponseDto(
        Long id,
        String sender,
        String content,
        String source,
        OffsetDateTime createdAt,
        Instant dateSent
) {}

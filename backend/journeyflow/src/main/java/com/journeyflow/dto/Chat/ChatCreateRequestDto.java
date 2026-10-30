package com.journeyflow.dto.Chat;

import java.time.Instant;
import java.time.OffsetDateTime;

public record ChatCreateRequestDto(
        String sender,
        String content,
        String source,
        Instant dateSent
) {}

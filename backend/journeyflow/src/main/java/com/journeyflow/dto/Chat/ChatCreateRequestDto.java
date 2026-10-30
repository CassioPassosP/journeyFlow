package com.journeyflow.dto.Chat;

import com.journeyflow.models.enums.ContentSource;
import com.journeyflow.models.enums.MessageSender;

import java.time.Instant;
import java.util.UUID;

public record ChatCreateRequestDto(
        UUID employeeId,
        MessageSender sender,
        String content,
        ContentSource source,
        String author,
        Instant dateSent
) {}
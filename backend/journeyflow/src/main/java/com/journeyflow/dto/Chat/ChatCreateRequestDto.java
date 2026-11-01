package com.journeyflow.dto.Chat;

import java.time.Instant;
import java.util.UUID;

import com.journeyflow.models.enums.ContentSource;
import com.journeyflow.models.enums.MessageSender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatCreateRequestDto(
        @NotNull UUID employeeId,
        @NotNull MessageSender sender,
        @NotBlank String content,
        ContentSource source,   // opcional: default TEXT
        Instant dateSent        // opcional: default agora
) {}
package com.journeyflow.dto.Chat;

import com.journeyflow.models.enums.ContentSource;
import com.journeyflow.models.enums.MessageSender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record ChatUpdateRequestDto(
        @NotNull UUID employeeId,
        @NotNull MessageSender sender,
        @NotBlank String content,
        ContentSource source,
        @NotBlank String author,
        @NotNull Instant dateSent
) {}
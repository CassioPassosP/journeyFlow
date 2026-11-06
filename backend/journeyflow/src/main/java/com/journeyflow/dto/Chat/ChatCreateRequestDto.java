package com.journeyflow.dto.Chat;

import java.time.Instant;
import java.util.UUID;

import com.journeyflow.models.enums.ContentSource;
import com.journeyflow.models.enums.MessageSender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatCreateRequestDto(
        @NotNull UUID employeeId,
        @NotNull MessageSender sender,
        @NotBlank @Size(max = 500) String content,
        ContentSource source,   // opcional: default TEXT
        Instant dateSent,       // opcional: default agora
        Long pillId             // opcional: vínculo com quiz de pílula
) {}

package com.journeyflow.dto.Chat;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.journeyflow.models.ChatEntity;

public record ChatResponseDto(
        Long id,
        String sender,
        String content,
        String source,
        OffsetDateTime createdAt,
        Instant dateSent
) {
    public ChatResponseDto(ChatEntity chat) {
        this(
                chat.getId(),
                chat.getSender().name(),
                chat.getContent(),
                chat.getSource().name(),
                chat.getCreatedAt(),
                chat.getDateSent()
        );
    }
}

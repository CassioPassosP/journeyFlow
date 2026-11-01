package com.journeyflow.dto.Chat;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.journeyflow.models.ChatEntity;
import com.journeyflow.models.enums.ContentSource;
import com.journeyflow.models.enums.MessageSender;

public record ChatResponseDto(
        Long id,
        UUID employeeId,
        MessageSender sender,
        String content,
        ContentSource source,
        String author,
        OffsetDateTime createdAt,
        Instant dateSent
) {
    public static ChatResponseDto from(ChatEntity c) {
        return new ChatResponseDto(
                c.getId(),
                c.getEmployee().getId(),
                c.getSender(),
                c.getContent(),
                c.getSource(),
                c.getAuthor(),
                c.getCreatedAt(),
                c.getDateSent());
    }
}
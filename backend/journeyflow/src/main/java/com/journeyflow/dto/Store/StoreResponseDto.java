package com.journeyflow.dto.Store;

import java.time.LocalDateTime;
import java.time.ZoneId;

import com.journeyflow.models.StoreEntity;

public record StoreResponseDto(
        Long id,
        String storeName,
        LocalDateTime creationDate
) {
    public static StoreResponseDto from(StoreEntity store) {
        LocalDateTime creationDate = store.getCreationDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return new StoreResponseDto(store.getId(), store.getStoreName(), creationDate);
    }
}
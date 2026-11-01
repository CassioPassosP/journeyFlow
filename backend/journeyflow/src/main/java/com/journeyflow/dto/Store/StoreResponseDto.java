package com.journeyflow.dto.Store;

import java.time.LocalDateTime;
import com.journeyflow.models.StoreEntity;

public record StoreResponseDto(
        Long id,
        String storeName,
        LocalDateTime creationDate
) {
    public static StoreResponseDto from(StoreEntity store) {
        LocalDateTime creationDate = store.getCreationDate();
        return new StoreResponseDto(store.getId(), store.getStoreName(), creationDate);
    }
}
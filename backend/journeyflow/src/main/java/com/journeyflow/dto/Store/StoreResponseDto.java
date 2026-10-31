package com.journeyflow.dto.Store;

import com.journeyflow.models.StoreEntity;

import java.util.Date;

public record StoreResponseDto(
        Long id,
        String storeName,
        Date creationDate
) {
    public StoreResponseDto(StoreEntity store) {
        this(
                store.getId(),
                store.getStoreName(),
                store.getCreationDate()
        );
    }
}

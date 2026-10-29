package com.journeyflow.dto.Store;

import java.util.Date;

 public record StoreResponseDto(
        Long id,
        String storeName,
        Date creationDate
)
{}

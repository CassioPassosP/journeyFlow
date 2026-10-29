package com.journeyflow.dto.Store;

import java.util.Date;

public record StoreCreateRequestDto(
        String storeName,
        Date creationDate
) {}

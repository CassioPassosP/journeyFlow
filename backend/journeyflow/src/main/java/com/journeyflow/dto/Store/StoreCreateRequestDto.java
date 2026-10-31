package com.journeyflow.dto.Store;

import jakarta.validation.constraints.NotBlank;

import java.util.Date;

public record StoreCreateRequestDto(
        @NotBlank String storeName,
        Date creationDate
) {}
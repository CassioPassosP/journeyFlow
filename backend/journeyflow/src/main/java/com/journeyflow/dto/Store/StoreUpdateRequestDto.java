package com.journeyflow.dto.Store;

import jakarta.validation.constraints.NotBlank;

public record StoreUpdateRequestDto(
        Long id,
        @NotBlank String storeName
) {}
package com.journeyflow.dto.Store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StoreCreateRequestDto(
        @NotBlank @Size(max = 150) String storeName
) {}
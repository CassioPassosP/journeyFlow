package com.journeyflow.dto.Store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// O id vem do path (PUT /store/{id});
public record StoreUpdateRequestDto(
        @NotBlank @Size(max = 150) String storeName
) {}
package com.journeyflow.dto.Manager;

import com.journeyflow.models.StoreEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ManagerUpdateRequestDto(
        Long id,
        @NotNull StoreEntity store,
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String position
) {}
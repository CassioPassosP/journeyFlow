package com.journeyflow.dto.Manager;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ManagerCreateRequestDto(
        @NotNull Long storeId,
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 8, max = 72) String password, // 72 bytes = limite do BCrypt
        @NotBlank @Size(max = 100) String position
) {
    // Evita que a senha apareça em logs. 
    @Override
    public String toString() {
        return "ManagerCreateRequestDto[storeId=%s, name=%s, email=%s, password=***]".formatted(storeId, name, email);
    }
}
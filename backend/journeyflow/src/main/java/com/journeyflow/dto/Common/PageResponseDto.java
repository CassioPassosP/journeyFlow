package com.journeyflow.dto.Common;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;


public record PageResponseDto<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <E, T> PageResponseDto<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponseDto<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}

/**
 * Envelope de paginação estável para a API (não expõe o PageImpl do Spring Data,
 * cujo JSON não é garantido entre versões).
 * O mapeamento entidade -> DTO deve acontecer dentro do @Transactional do service.
 */
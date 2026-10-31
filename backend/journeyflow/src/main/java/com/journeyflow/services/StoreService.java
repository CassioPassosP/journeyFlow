package com.journeyflow.services;

import com.journeyflow.dto.Store.StoreCreateRequestDto;
import com.journeyflow.dto.Store.StoreResponseDto;
import com.journeyflow.dto.Store.StoreUpdateRequestDto;
import com.journeyflow.models.StoreEntity;
import com.journeyflow.repositories.StoreRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional
    public StoreResponseDto createStore(StoreCreateRequestDto dto) {
        if (dto.storeName() == null || dto.storeName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nome da loja e obrigatorio");
        }
        StoreEntity store = new StoreEntity();

        store.setStoreName(dto.storeName());
        store.setCreationDate(dto.creationDate());

        return new StoreResponseDto(storeRepository.save(store));
    }

    @Transactional
    public Optional<StoreResponseDto> updateStore(Long id, @Valid StoreUpdateRequestDto dto) {
        StoreEntity store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Loja nao encontrada"));

        if (dto.storeName() == null || dto.storeName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nome da loja e obrigatorio");
        }
        store.setStoreName(dto.storeName());

        return Optional.of(new StoreResponseDto(storeRepository.save(store)));
    }

    @Transactional
    public void deleteStore(Long id) {
        StoreEntity store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Loja nao encontrada"));

        storeRepository.delete(store);
    }

    @Transactional
    public List<StoreResponseDto> findAllStores() {
        return storeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StoreResponseDto findStoreById(Long id) {
        StoreEntity store = storeRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Loja nao encontrada"));

        return toResponse(store);
    }

    private StoreResponseDto toResponse(StoreEntity store) {
        return new StoreResponseDto(store.getId(), store.getStoreName(), store.getCreationDate());
    }
}
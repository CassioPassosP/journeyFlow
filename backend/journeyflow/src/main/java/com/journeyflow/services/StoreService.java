package com.journeyflow.services;

import com.journeyflow.dto.Store.StoreCreateRequestDto;
import com.journeyflow.dto.Store.StoreResponseDto;
import com.journeyflow.dto.Store.StoreUpdateRequestDto;
import com.journeyflow.models.StoreEntity;
import com.journeyflow.repositories.StoreRepository;
import jakarta.transaction.Transactional;
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
        StoreEntity store = new StoreEntity();
        applyFields(store, dto.storeName());
        return new StoreResponseDto(storeRepository.save(store));
    }

    @Transactional
    public Optional<StoreResponseDto> updateStore(Long id, StoreUpdateRequestDto dto) {
        StoreEntity store = storeRepository.getOne(id);
        if (store.getId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Loja nao encontrada");
        }

        applyFields(store, dto.storeName());
        return Optional.of(new StoreResponseDto(storeRepository.save(store)));
    }

    @Transactional
    public void deleteStore(Long id) {
        StoreEntity store = storeRepository.getOne(id);
        storeRepository.delete(store);
    }

    @Transactional
    public List<StoreResponseDto> findAllStores() {
        return storeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public StoreResponseDto findStoreById(Long id) {
        StoreEntity store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loja nao encontrada"));
        return toResponse(store);
    }

    // Copia o nome recebido no DTO para a entidade da loja.
    private void applyFields(StoreEntity store, String storeName) {
        store.setStoreName(storeName);
    }

    private StoreResponseDto toResponse(StoreEntity store) {
        return new StoreResponseDto(store.getId(), store.getStoreName(), store.getCreationDate());
    }
}
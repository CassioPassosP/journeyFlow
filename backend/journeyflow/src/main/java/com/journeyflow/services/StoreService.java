package com.journeyflow.services;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.journeyflow.dto.Store.StoreCreateRequestDto;
import com.journeyflow.dto.Store.StoreResponseDto;
import com.journeyflow.dto.Store.StoreUpdateRequestDto;
import com.journeyflow.models.StoreEntity;
import com.journeyflow.repositories.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {

    private static final Sort DEFAULT_SORT = Sort.by("storeName");

    private final StoreRepository storeRepository;

    @Transactional
    public StoreResponseDto createStore(StoreCreateRequestDto dto) {
        StoreEntity store = new StoreEntity();
        store.setStoreName(dto.storeName().trim());
        // creationDate é preenchida pelo @PrePersist da entidade

        return StoreResponseDto.from(storeRepository.save(store));
    }

    @Transactional
    public StoreResponseDto updateStore(Long id, StoreUpdateRequestDto dto) {
        StoreEntity store = findStore(id);
        store.setStoreName(dto.storeName().trim());

        return StoreResponseDto.from(store); // entidade gerenciada: o dirty checking grava no commit
    }

    @Transactional
    public void deleteStore(Long id) {
        StoreEntity store = findStore(id);
        storeRepository.delete(store);
        storeRepository.flush(); // FK violada (loja com colaboradores) vira 409 no GlobalExceptionHandler
    }

    @Transactional(readOnly = true)
    public List<StoreResponseDto> findAllStores() {
        return storeRepository.findAll(DEFAULT_SORT)
                .stream()
                .map(StoreResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StoreResponseDto findStoreById(Long id) {
        return StoreResponseDto.from(findStore(id));
    }

    private StoreEntity findStore(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loja não encontrada"));
    }
}
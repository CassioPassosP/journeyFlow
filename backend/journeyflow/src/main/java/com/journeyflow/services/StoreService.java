package com.journeyflow.services;

import com.journeyflow.dto.Store.StoreCreateRequestDto;
import com.journeyflow.dto.Store.StoreResponseDto;
import com.journeyflow.dto.Store.StoreUpdateRequestDto;
import com.journeyflow.models.StoreEntity;
import com.journeyflow.repositories.StoreRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    //create
    @Transactional
    public StoreResponseDto createStore(StoreCreateRequestDto dto) {
        StoreEntity store = new StoreEntity();

        store.setStoreName(dto.storeName());

        StoreEntity saved = storeRepository.save(store);
        return new StoreResponseDto(saved);
    }

    //update
    @Transactional
    public Optional<StoreResponseDto> updateStore(Long id,StoreUpdateRequestDto dto) {
        StoreEntity store = storeRepository.getOne(id);

        store.setStoreName(dto.storeName());

        StoreEntity saved = storeRepository.save(store);
        return Optional.of(new StoreResponseDto(saved));
    }

    //delete
    @Transactional
    public void deleteStore(Long id) {
        StoreEntity store = storeRepository.getOne(id);

        storeRepository.delete(store);
    }

    //find all
    @Transactional
    public List<StoreResponseDto> findAllStores(){
        return storeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    //find by id
    @Transactional
    public StoreResponseDto findStoreById(Long id) {
        StoreEntity store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Loja não encontrada"
                ));

        return toResponse(store);
    }

    // Converte Entity para Response DTO
    private StoreResponseDto toResponse(StoreEntity store) {

        return new StoreResponseDto(
                store.getId(),
                store.getStoreName(),
                store.getCreationDate()
        );
    }

}

package com.journeyflow.services;

import com.journeyflow.dto.Store.StoreCreateRequestDto;
import com.journeyflow.dto.Store.StoreResponseDto;
import com.journeyflow.dto.Store.StoreUpdateRequestDto;
import com.journeyflow.models.StoreEntity;
import com.journeyflow.repositories.StoreRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Transactional
    public StoreResponseDto createStore(StoreCreateRequestDto dto) {
        StoreEntity store = new StoreEntity();

        store.setStoreName(dto.storeName());

        StoreEntity saved = storeRepository.save(store);
        return new StoreResponseDto(saved);
    }

    @Transactional
    public Optional<StoreResponseDto> updateStore(Long id,StoreUpdateRequestDto dto) {
        StoreEntity store = storeRepository.getOne(id);

        store.setStoreName(dto.storeName());

        StoreEntity saved = storeRepository.save(store);
        return Optional.of(new StoreResponseDto(saved));
    }

    @Transactional
    public void deleteStore(Long id) {
        StoreEntity store = storeRepository.getOne(id);

        storeRepository.delete(store);
    }

}

package com.journeyflow.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.journeyflow.dto.Store.StoreCreateRequestDto;
import com.journeyflow.dto.Store.StoreResponseDto;
import com.journeyflow.dto.Store.StoreUpdateRequestDto;
import com.journeyflow.services.StoreService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StoreResponseDto create(@Valid @RequestBody StoreCreateRequestDto request) {
        return storeService.createStore(request);
    }

    @GetMapping
    public List<StoreResponseDto> findAll() {
        return storeService.findAllStores();
    }

    @GetMapping("/{id}")
    public StoreResponseDto findById(@PathVariable Long id) {
        return storeService.findStoreById(id);
    }

    @PutMapping("/{id}")
    public StoreResponseDto update(@PathVariable Long id, @Valid @RequestBody StoreUpdateRequestDto request) {
        return storeService.updateStore(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        storeService.deleteStore(id);
    }
}
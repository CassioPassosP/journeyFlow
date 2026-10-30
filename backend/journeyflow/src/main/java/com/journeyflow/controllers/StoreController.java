package com.journeyflow.controllers;

import com.journeyflow.dto.Reports.ReportsCreateRequestDto;
import com.journeyflow.dto.Reports.ReportsResponseDto;
import com.journeyflow.dto.Store.StoreCreateRequestDto;
import com.journeyflow.dto.Store.StoreResponseDto;
import com.journeyflow.services.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    @Autowired
    private StoreService storeService;

    //create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StoreResponseDto create(
            @Valid @RequestBody StoreCreateRequestDto request) {

        return storeService.createStore(request);
    }

    //find all
    @GetMapping
    public List<StoreResponseDto> findAll() {

        return storeService.findAllStores();
    }

    //find by id
    @GetMapping("/{id}")
    public StoreResponseDto findById(@PathVariable Long id) {

        return storeService.findStoreById(id);
    }

    //update
    @PutMapping("/{id}")
    public Optional<StoreResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody StoreCreateRequestDto request) {

        return storeService.updateStore(id, request);
    }

    //delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        storeService.deleteStore(id);
    }
}

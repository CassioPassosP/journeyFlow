package com.journeyflow.controllers;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.journeyflow.dto.Common.PageResponseDto;
import com.journeyflow.dto.Manager.ManagerCreateRequestDto;
import com.journeyflow.dto.Manager.ManagerResponseDto;
import com.journeyflow.dto.Manager.ManagerUpdateRequestDto;
import com.journeyflow.services.ManagerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ManagerResponseDto create(@Valid @RequestBody ManagerCreateRequestDto request) {
        return managerService.createManager(request);
    }

    /** GET /manager?storeId=1&page=0&size=20 (ordenado por nome). */
    @GetMapping
    public PageResponseDto<ManagerResponseDto> findAll(
            @RequestParam(required = false) Long storeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return managerService.findManagers(storeId, pageable);
    }

    @GetMapping("/{id}")
    public ManagerResponseDto findById(@PathVariable Long id) {
        return managerService.findManagerById(id);
    }

    @PutMapping("/{id}")
    public ManagerResponseDto update(@PathVariable Long id, @Valid @RequestBody ManagerUpdateRequestDto request) {
        return managerService.updateManager(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        managerService.deleteManager(id);
    }
}
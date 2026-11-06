package com.journeyflow.controllers;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.journeyflow.dto.Alert.AlertAssignRequestDto;
import com.journeyflow.dto.Alert.AlertConfirmedUpdateRequestDto;
import com.journeyflow.dto.Alert.AlertCreateRequestDto;
import com.journeyflow.dto.Alert.AlertResponseDto;
import com.journeyflow.dto.Alert.AlertStatusUpdateRequestDto;
import com.journeyflow.dto.Common.PageResponseDto;
import com.journeyflow.models.enums.AlertStatus;
import com.journeyflow.services.AlertService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/alert")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertResponseDto create(@Valid @RequestBody AlertCreateRequestDto request, Authentication authentication) {
        return alertService.createAlert(request, authentication.getName());
    }

    /** A unidade e o destino são limitados pelo gestor autenticado; storeId, se informado, precisa corresponder. */
    @GetMapping
    public PageResponseDto<AlertResponseDto> findAll(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) AlertStatus status,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        return alertService.findAlerts(storeId, status, pageable, authentication.getName());
    }

    @GetMapping("/{id}")
    public AlertResponseDto findById(@PathVariable Long id, Authentication authentication) {
        return alertService.findAlertById(id, authentication.getName());
    }

    @PatchMapping("/{id}/status")
    public AlertResponseDto updateStatus(@PathVariable Long id,
                                         @Valid @RequestBody AlertStatusUpdateRequestDto request,
                                         Authentication authentication) {
        return alertService.updateStatus(id, request, authentication.getName());
    }

    @PatchMapping("/{id}/confirmed")
    public AlertResponseDto updateConfirmed(@PathVariable Long id,
                                            @Valid @RequestBody AlertConfirmedUpdateRequestDto request,
                                            Authentication authentication) {
        return alertService.updateConfirmed(id, request, authentication.getName());
    }

    @PatchMapping("/{id}/assign")
    public AlertResponseDto assign(@PathVariable Long id, @Valid @RequestBody AlertAssignRequestDto request,
                                   Authentication authentication) {
        return alertService.assignManager(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        alertService.deleteAlert(id, authentication.getName());
    }
}
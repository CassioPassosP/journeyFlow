package com.journeyflow.controllers;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
    public AlertResponseDto create(@Valid @RequestBody AlertCreateRequestDto request) {
        return alertService.createAlert(request);
    }

    /** GET /alert?storeId=1&status=OPEN&page=0&size=20 (a ordenação é fixa: mais recentes primeiro). */
    @GetMapping
    public PageResponseDto<AlertResponseDto> findAll(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) AlertStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return alertService.findAlerts(storeId, status, pageable);
    }

    @GetMapping("/{id}")
    public AlertResponseDto findById(@PathVariable Long id) {
        return alertService.findAlertById(id);
    }

    @PatchMapping("/{id}/status")
    public AlertResponseDto updateStatus(@PathVariable Long id,
                                         @Valid @RequestBody AlertStatusUpdateRequestDto request) {
        return alertService.updateStatus(id, request);
    }

    @PatchMapping("/{id}/confirmed")
    public AlertResponseDto updateConfirmed(@PathVariable Long id,
                                            @Valid @RequestBody AlertConfirmedUpdateRequestDto request) {
        return alertService.updateConfirmed(id, request);
    }

    @PatchMapping("/{id}/assign")
    public AlertResponseDto assign(@PathVariable Long id, @Valid @RequestBody AlertAssignRequestDto request) {
        return alertService.assignManager(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        alertService.deleteAlert(id);
    }
}
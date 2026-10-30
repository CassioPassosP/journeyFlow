package com.journeyflow.controllers;

import com.journeyflow.dto.Alert.AlertCreateRequestDto;
import com.journeyflow.dto.Alert.AlertResponseDto;
import com.journeyflow.services.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alert")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    //create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertResponseDto create(
            @Valid @RequestBody AlertCreateRequestDto request) {

        return alertService.createAlert(request);
    }

    //find all
    @GetMapping
    public List<AlertResponseDto> findAll() {

        return alertService.findAllAlerts();
    }

    //find by id
    @GetMapping("/{id}")
    public AlertResponseDto findById(@PathVariable Long id) {

        return alertService.findAlertById(id);
    }

    //delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        alertService.deleteAlert(id);
    }

}

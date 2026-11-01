package com.journeyflow.controllers;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.journeyflow.dto.Common.PageResponseDto;
import com.journeyflow.dto.Employee.EmployeeConsentRequestDto;
import com.journeyflow.dto.Employee.EmployeeCreateRequestDto;
import com.journeyflow.dto.Employee.EmployeeResponseDto;
import com.journeyflow.dto.Employee.EmployeeUpdateRequestDto;
import com.journeyflow.services.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponseDto create(@Valid @RequestBody EmployeeCreateRequestDto request) {
        return employeeService.createEmployee(request);
    }

    /** GET /employee?storeId=1&page=0&size=20 (ordenado por nome). */
    @GetMapping
    public PageResponseDto<EmployeeResponseDto> findAll(
            @RequestParam(required = false) Long storeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return employeeService.findEmployees(storeId, pageable);
    }

    @GetMapping("/{id}")
    public EmployeeResponseDto findById(@PathVariable UUID id) {
        return employeeService.findEmployeeById(id);
    }

    @PutMapping("/{id}")
    public EmployeeResponseDto update(@PathVariable UUID id, @Valid @RequestBody EmployeeUpdateRequestDto request) {
        return employeeService.updateEmployee(id, request);
    }

    @PatchMapping("/{id}/consent")
    public EmployeeResponseDto updateConsent(@PathVariable UUID id,
                                             @Valid @RequestBody EmployeeConsentRequestDto request) {
        return employeeService.updateConsent(id, request.consent());
    }

    /** Registra a interação de hoje e atualiza o streak. */
    @PostMapping("/{id}/activity")
    public EmployeeResponseDto registerActivity(@PathVariable UUID id) {
        return employeeService.registerActivity(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
    }
}
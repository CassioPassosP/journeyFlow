package com.journeyflow.controllers;

import com.journeyflow.dto.Employee.EmployeeCreateRequestDto;
import com.journeyflow.dto.Employee.EmployeeResponseDto;
import com.journeyflow.dto.Employee.EmployeeUpdateRequestDto;
import com.journeyflow.services.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping
    public List<EmployeeResponseDto> findAll() {
        return employeeService.findAllEmployees();
    }

    @GetMapping("/{id}")
    public EmployeeResponseDto findById(@PathVariable UUID id) {
        return employeeService.findEmployeeById(id);
    }

    @PutMapping("/{id}")
    public EmployeeResponseDto update(@PathVariable UUID id, @Valid @RequestBody EmployeeUpdateRequestDto request) {
        return employeeService.updateEmployee(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
    }
}
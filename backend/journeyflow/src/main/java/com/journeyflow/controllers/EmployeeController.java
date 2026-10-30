package com.journeyflow.controllers;

import com.journeyflow.dto.Chat.ChatCreateRequestDto;
import com.journeyflow.dto.Chat.ChatResponseDto;
import com.journeyflow.dto.Employee.EmployeeCreateRequestDto;
import com.journeyflow.dto.Employee.EmployeeResponseDto;
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

    //create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponseDto create(
            @Valid @RequestBody EmployeeCreateRequestDto request) {

        return employeeService.createEmployee(request);
    }

    //find all
    @GetMapping
    public List<EmployeeResponseDto> findAll() {

        return employeeService.findAllEmployees();
    }

    //find by id
    @GetMapping("/{id}")
    public EmployeeResponseDto findById(@PathVariable UUID id) {

        return employeeService.findEmployeeById(id);
    }

    //update
    @PutMapping("/{id}")
    public EmployeeResponseDto update(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeCreateRequestDto request) {

        return employeeService.updateEmployee(id, request);
    }

    //delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {

        employeeService.deleteEmployee(id);
    }
}

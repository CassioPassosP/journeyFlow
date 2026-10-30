package com.journeyflow.controllers;

import com.journeyflow.dto.Employee.EmployeeCreateRequestDto;
import com.journeyflow.dto.Employee.EmployeeResponseDto;
import com.journeyflow.dto.Manager.ManagerCreateRequestDto;
import com.journeyflow.dto.Manager.ManagerResponseDto;
import com.journeyflow.services.ManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    //create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ManagerResponseDto create(
            @Valid @RequestBody ManagerCreateRequestDto request) {

        return managerService.createManager(request);
    }

    //find all
    @GetMapping
    public List<ManagerResponseDto> findAll() {

        return managerService.findAllManagers();
    }

    //find by id
    @GetMapping("/{id}")
    public ManagerResponseDto findById(@PathVariable Long id) {

        return managerService.findManagerById(id);
    }

    //update
    @PutMapping("/{id}")
    public ManagerResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody ManagerCreateRequestDto request) {

        return managerService.updateManager(id, request);
    }

    //delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        managerService.deleteManager(id);
    }

}

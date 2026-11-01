package com.journeyflow.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.journeyflow.dto.Common.PageResponseDto;
import com.journeyflow.dto.Points.PointsBalanceResponseDto;
import com.journeyflow.dto.Points.PointsCreateRequestDto;
import com.journeyflow.dto.Points.PointsResponseDto;
import com.journeyflow.dto.Points.PointsUpdateRequestDto;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.PointsEntity;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.PointsRepository;

import lombok.RequiredArgsConstructor;

// points_log é um histórico de lançamentos ; o saldo é a soma, calculada no banco. 
@Service
@RequiredArgsConstructor
public class PointsService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final PointsRepository pointsRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public PointsResponseDto createPoints(PointsCreateRequestDto dto) {
        assertNotZero(dto.points());
        EmployeeEntity employee = findEmployee(dto.employeeId());

        PointsEntity entry = new PointsEntity();
        entry.setEmployee(employee);
        entry.setReason(dto.reason().trim());
        entry.setPoints(dto.points());

        return PointsResponseDto.from(pointsRepository.save(entry));
    }

    // Só motivo e pontos mudam
    @Transactional
    public PointsResponseDto updatePoints(Long id, PointsUpdateRequestDto dto) {
        assertNotZero(dto.points());
        PointsEntity entry = findPoints(id);

        entry.setReason(dto.reason().trim());
        entry.setPoints(dto.points());

        return PointsResponseDto.from(entry);
    }

    @Transactional
    public void deletePoints(Long id) {
        pointsRepository.delete(findPoints(id));
    }

    @Transactional(readOnly = true)
    public PageResponseDto<PointsResponseDto> findPointsEntries(UUID employeeId, Pageable pageable) {
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);

        Page<PointsEntity> result = employeeId == null
                ? pointsRepository.findAll(page)
                : pointsRepository.findByEmployeeId(employeeId, page);

        return PageResponseDto.from(result, PointsResponseDto::from);
    }

    @Transactional(readOnly = true)
    public PointsResponseDto findPointsById(Long id) {
        return PointsResponseDto.from(findPoints(id));
    }

    @Transactional(readOnly = true)
    public PointsBalanceResponseDto getBalance(UUID employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado");
        }
        return new PointsBalanceResponseDto(employeeId, pointsRepository.sumPointsByEmployeeId(employeeId));
    }

    private PointsEntity findPoints(Long id) {
        return pointsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Registro de pontos não encontrado"));
    }

    private EmployeeEntity findEmployee(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));
    }

    private static void assertNotZero(Integer points) {
        if (points == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quantidade de pontos não pode ser zero");
        }
    }
}
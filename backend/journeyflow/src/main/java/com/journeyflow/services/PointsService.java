package com.journeyflow.services;

import com.journeyflow.dto.Points.PointsCreateRequestDto;
import com.journeyflow.dto.Points.PointsResponseDto;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.PointsEntity;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.PointsRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class PointsService {

    private final PointsRepository pointsRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public PointsResponseDto createPoints(PointsCreateRequestDto dto) {
        if (dto.reason() == null || dto.reason().isBlank() || dto.points() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Motivo e quantidade de pontos sao obrigatorios");
        }
        EmployeeEntity employee = findEmployee(dto.employee());

        PointsEntity points = new PointsEntity();

        points.setEmployee(employee);
        points.setReason(dto.reason());
        points.setPoints(dto.points());

        return new PointsResponseDto(pointsRepository.save(points));
    }

    @Transactional
    public PointsResponseDto updatePoints(Long id, @Valid PointsCreateRequestDto dto) {
        PointsEntity points = pointsRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Registro de pontos nao encontrado"));

        if (dto.employee() != null) points.setEmployee(findEmployee(dto.employee()));
        if (dto.points() != null) points.setPoints(dto.points());

        return new PointsResponseDto(pointsRepository.save(points));
    }

    @Transactional
    public void deletePoints(Long id) {
        PointsEntity points = pointsRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Registro de pontos nao encontrado"));

        pointsRepository.delete(points);
    }

    @Transactional
    public List<PointsResponseDto> findAllPoints() {
        return pointsRepository.findAll()
                .stream()
                .map(PointsResponseDto::new)
                .toList();
    }

    @Transactional
    public PointsResponseDto findPointsById(Long id) {
        return pointsRepository.findById(id)
                .map(PointsResponseDto::new)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registro de pontos nao encontrado"));
    }

    private EmployeeEntity findEmployee(EmployeeEntity employeeDto) {
        if (employeeDto == null || employeeDto.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID do funcionario e obrigatorio");
        }
        return employeeRepository.findById(employeeDto.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Funcionario nao encontrado"));
    }
}
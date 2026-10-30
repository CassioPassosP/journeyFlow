package com.journeyflow.services;

import com.journeyflow.dto.Points.PointsCreateRequestDto;
import com.journeyflow.dto.Points.PointsResponseDto;
import com.journeyflow.dto.Points.PointsUpdateRequestDto;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.PointsEntity;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.PointsRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class PointsService {

    private final PointsRepository pointsRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public PointsResponseDto createPoints(PointsCreateRequestDto dto) {
        EmployeeEntity employee = findEmployee(dto.employee());
        PointsEntity points = new PointsEntity();
        points.setReason(dto.reason());
        applyFields(points, employee, dto.points());
        return new PointsResponseDto(pointsRepository.save(points));
    }

    @Transactional
    public PointsResponseDto updatePoints(Long id, PointsUpdateRequestDto dto) {
        PointsEntity points = pointsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registro de pontos nao encontrado"
                ));

        EmployeeEntity employee = dto.employee() == null
                ? points.getEmployee()
                : findEmployee(dto.employee());
        Integer amount = dto.points() == null ? points.getPoints() : dto.points();
        applyFields(points, employee, amount);

        return new PointsResponseDto(pointsRepository.save(points));
    }

    @Transactional
    public void deletePoints(Long id) {
        PointsEntity points = pointsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registro de pontos nao encontrado"
                ));
        pointsRepository.delete(points);
    }

    // Aplica o funcionario e a quantidade de pontos na entidade.
    private void applyFields(PointsEntity points, EmployeeEntity employee, Integer amount) {
        points.setEmployee(employee);
        points.setPoints(amount);
    }

    private EmployeeEntity findEmployee(EmployeeEntity employeeDto) {
        if (employeeDto == null || employeeDto.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID do funcionario e obrigatorio");
        }
        return employeeRepository.findById(employeeDto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionario nao encontrado"));
    }
}
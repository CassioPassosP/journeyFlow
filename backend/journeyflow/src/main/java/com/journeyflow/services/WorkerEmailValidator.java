package com.journeyflow.services;

import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.ManagerRepository;

import lombok.RequiredArgsConstructor;

/**
 * O login (AuthService) procura o e-mail em colaboradores e gerentes ao mesmo tempo,
 * então o e-mail precisa ser único ENTRE as duas tabelas, não só dentro de cada uma.
 * Centralizar a regra aqui evita duplicá-la em EmployeeService e ManagerService.
 */
@Component
@RequiredArgsConstructor
public class WorkerEmailValidator {

    private final EmployeeRepository employeeRepository;
    private final ManagerRepository managerRepository;

    // Padroniza o e-mail antes de gravar/consultar (trim + minúsculas). 
    public static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * @param email             e-mail já normalizado
     * @param ignoreEmployeeId  colaborador que está sendo editado (ou null ao criar)
     * @param ignoreManagerId   gerente que está sendo editado (ou null ao criar)
     */
    public void assertAvailable(String email, UUID ignoreEmployeeId, Long ignoreManagerId) {
        boolean takenByEmployee = ignoreEmployeeId == null
                ? employeeRepository.existsByEmailIgnoreCase(email)
                : employeeRepository.existsByEmailIgnoreCaseAndIdNot(email, ignoreEmployeeId);

        boolean takenByManager = ignoreManagerId == null
                ? managerRepository.existsByEmailIgnoreCase(email)
                : managerRepository.existsByEmailIgnoreCaseAndIdNot(email, ignoreManagerId);

        if (takenByEmployee || takenByManager) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
    }
}
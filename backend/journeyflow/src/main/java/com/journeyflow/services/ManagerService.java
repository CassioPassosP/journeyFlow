package com.journeyflow.services;

import com.journeyflow.dto.Manager.ManagerCreateRequestDto;
import com.journeyflow.dto.Manager.ManagerResponseDto;
import com.journeyflow.dto.Manager.ManagerUpdateRequestDto;
import com.journeyflow.models.ManagerEntity;
import com.journeyflow.models.StoreEntity;
import com.journeyflow.repositories.ManagerRepository;
import com.journeyflow.repositories.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ManagerResponseDto createManager(ManagerCreateRequestDto dto) {
        if (dto.store() == null || dto.store().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID da loja e obrigatorio");
        }
        if (dto.name() == null || dto.name().isBlank() || dto.email() == null || dto.email().isBlank()
                || dto.position() == null || dto.position().isBlank() || dto.password() == null || dto.password().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nome, email, cargo e senha sao obrigatorios");
        }
        StoreEntity store = storeRepository.findById(dto.store().getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Loja nao encontrada"));

        ManagerEntity manager = new ManagerEntity();
        manager.setStore(store);
        manager.setName(dto.name());
        manager.setEmail(dto.email());
        manager.setPassword(passwordEncoder.encode(dto.password()));
        manager.setPosition(dto.position());
        return new ManagerResponseDto(managerRepository.save(manager));
    }

    @Transactional
    public ManagerResponseDto updateManager(Long id, ManagerUpdateRequestDto dto) {
        ManagerEntity manager = managerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gerente nao encontrado"));
        if (dto.store() == null || dto.store().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID da loja e obrigatorio");
        }
        if (dto.name() == null || dto.name().isBlank() || dto.email() == null || dto.email().isBlank()
                || dto.position() == null || dto.position().isBlank() || dto.password() == null || dto.password().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nome, email, cargo e senha sao obrigatorios");
        }
        StoreEntity store = storeRepository.findById(dto.store().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Loja nao encontrada"));

        manager.setStore(store);
        manager.setName(dto.name());
        manager.setEmail(dto.email());
        manager.setPassword(passwordEncoder.encode(dto.password()));
        manager.setPosition(dto.position());
        return new ManagerResponseDto(managerRepository.save(manager));
    }

    @Transactional
    public void deleteManager(Long id) {
        ManagerEntity manager = managerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Gerente nao encontrado"));
        managerRepository.delete(manager);
    }

    @Transactional
    public List<ManagerResponseDto> findAllManagers() {
        return managerRepository.findAll()
                .stream()
                .map(ManagerResponseDto::new)
                .toList();
    }

    @Transactional
    public ManagerResponseDto findManagerById(Long id) {
        return managerRepository.findById(id)
                .map(ManagerResponseDto::new)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Gerente nao encontrado"));
    }
}
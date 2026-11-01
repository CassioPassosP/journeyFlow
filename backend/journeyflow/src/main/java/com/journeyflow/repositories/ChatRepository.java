package com.journeyflow.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.journeyflow.models.ChatEntity;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    Page<ChatEntity> findByEmployeeIdOrderByDateSentDesc(UUID employeeId, Pageable pageable);
}
package com.journeyflow.repositories;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.journeyflow.models.ChatEntity;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    Page<ChatEntity> findByEmployeeIdOrderByDateSentDesc(UUID employeeId, Pageable pageable);

    List<ChatEntity> findByEmployeeIdOrderByDateSentAsc(UUID employeeId);

    Optional<ChatEntity> findFirstByEmployeeIdOrderByDateSentDesc(UUID employeeId);

    Optional<ChatEntity> findFirstByEmployeeIdAndSenderAndPillIdOrderByDateSentDesc(
            UUID employeeId, com.journeyflow.models.enums.MessageSender sender, Long pillId);
}

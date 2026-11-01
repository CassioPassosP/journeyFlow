package com.journeyflow.services;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.journeyflow.dto.Chat.ChatCreateRequestDto;
import com.journeyflow.dto.Chat.ChatResponseDto;
import com.journeyflow.dto.Chat.ChatUpdateRequestDto;
import com.journeyflow.dto.Common.PageResponseDto;
import com.journeyflow.models.ChatEntity;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.enums.ContentSource;
import com.journeyflow.models.enums.MessageSender;
import com.journeyflow.repositories.ChatRepository;
import com.journeyflow.repositories.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    /** Nome gravado em "author" para mensagens enviadas pelo bot. */
    static final String BOT_AUTHOR = "JourneyFlow";

    private final ChatRepository chatRepository;
    private final EmployeeRepository employeeRepository;
    private final Clock clock;

    /**
     * O DTO de criação não recebe "author": ele é derivado aqui
     * (nome do colaborador para USER, BOT_AUTHOR para BOT), então o cliente não consegue forjá-lo.
     * dateSent é opcional e assume "agora".
     */
    @Transactional
    public ChatResponseDto createChat(ChatCreateRequestDto dto) {
        EmployeeEntity employee = findEmployee(dto.employeeId());

        ChatEntity chat = new ChatEntity();
        chat.setEmployee(employee);
        chat.setSender(dto.sender());
        chat.setContent(dto.content().trim());
        chat.setSource(dto.source() == null ? ContentSource.TEXT : dto.source());
        chat.setAuthor(resolveAuthor(dto.sender(), employee));
        chat.setDateSent(dto.dateSent() == null ? Instant.now(clock) : dto.dateSent());

        return ChatResponseDto.from(chatRepository.save(chat));
    }

    @Transactional
    public ChatResponseDto updateChat(Long id, ChatUpdateRequestDto dto) {
        ChatEntity chat = findChat(id);
        EmployeeEntity employee = findEmployee(dto.employeeId());

        chat.setEmployee(employee);
        chat.setSender(dto.sender());
        chat.setContent(dto.content().trim());
        chat.setSource(dto.source() == null ? ContentSource.TEXT : dto.source());
        chat.setAuthor(dto.author().trim());
        chat.setDateSent(dto.dateSent());

        return ChatResponseDto.from(chat);
    }

    @Transactional
    public void deleteChat(Long id) {
        chatRepository.delete(findChat(id));
    }

    /** Com employeeId: histórico da conversa (mais recentes primeiro). Sem: todas as mensagens, também desc. */
    @Transactional(readOnly = true)
    public PageResponseDto<ChatResponseDto> findChats(UUID employeeId, Pageable pageable) {
        int number = pageable.getPageNumber();
        int size = pageable.getPageSize();

        Page<ChatEntity> result = employeeId == null
                ? chatRepository.findAll(PageRequest.of(number, size, Sort.by(Sort.Direction.DESC, "dateSent")))
                : chatRepository.findByEmployeeIdOrderByDateSentDesc(employeeId, PageRequest.of(number, size));

        return PageResponseDto.from(result, ChatResponseDto::from);
    }

    @Transactional(readOnly = true)
    public ChatResponseDto findChatById(Long id) {
        return ChatResponseDto.from(findChat(id));
    }

    private ChatEntity findChat(Long id) {
        return chatRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada"));
    }

    private EmployeeEntity findEmployee(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));
    }

    private static String resolveAuthor(MessageSender sender, EmployeeEntity employee) {
        return sender == MessageSender.BOT ? BOT_AUTHOR : employee.getName();
    }
}
package com.journeyflow.services;

import com.journeyflow.dto.Chat.ChatCreateRequestDto;
import com.journeyflow.dto.Chat.ChatResponseDto;
import com.journeyflow.models.ChatEntity;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.enums.ContentSource;
import com.journeyflow.repositories.ChatRepository;
import com.journeyflow.repositories.EmployeeRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public ChatResponseDto createChat(ChatCreateRequestDto dto) {
        if (dto.employeeId() == null || dto.sender() == null || dto.content() == null || dto.content().isBlank()
                || dto.author() == null || dto.author().isBlank() || dto.dateSent() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Campos obrigatorios da mensagem estao ausentes");
        }
        EmployeeEntity employee = employeeRepository.findById(dto.employeeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Funcionario nao encontrado"));

        ChatEntity chat = new ChatEntity();

        chat.setEmployee(employee);
        chat.setSender(dto.sender());
        chat.setContent(dto.content());
        chat.setSource(dto.source() == null ? ContentSource.TEXT : dto.source());
        chat.setAuthor(dto.author());
        chat.setDateSent(dto.dateSent());

        return new ChatResponseDto(chatRepository.save(chat));
    }

    @Transactional
    public ChatResponseDto updateChat(Long id, @Valid ChatCreateRequestDto dto) {
        ChatEntity chat = chatRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem nao encontrada"));
        if (dto.employeeId() == null || dto.sender() == null || dto.content() == null || dto.content().isBlank()
                || dto.author() == null || dto.author().isBlank() || dto.dateSent() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Campos obrigatorios da mensagem estao ausentes");
        }
        EmployeeEntity employee = employeeRepository.findById(dto.employeeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Funcionario nao encontrado"));

        chat.setEmployee(employee);
        chat.setSender(dto.sender());
        chat.setContent(dto.content());
        chat.setSource(dto.source() == null ? ContentSource.TEXT : dto.source());
        chat.setAuthor(dto.author());
        chat.setDateSent(dto.dateSent());

        return new ChatResponseDto(chatRepository.save(chat));
    }

    @Transactional
    public void deleteChat(Long id) {
        ChatEntity chat = chatRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Mensagem nao encontrada"));

        chatRepository.delete(chat);
    }

    @Transactional
    public List<ChatResponseDto> findAllChats() {
        return chatRepository.findAll()
                .stream()
                .map(ChatResponseDto::new)
                .toList();
    }

    @Transactional
    public ChatResponseDto findChatById(Long id) {
        return chatRepository.findById(id)
                .map(ChatResponseDto::new)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Mensagem nao encontrada"));
    }
}
package com.journeyflow.services;

import com.journeyflow.dto.Chat.ChatCreateRequestDto;
import com.journeyflow.dto.Chat.ChatResponseDto;
import com.journeyflow.dto.Chat.ChatUpdateRequestDto;
import com.journeyflow.models.ChatEntity;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.enums.ContentSource;
import com.journeyflow.models.enums.MessageSender;
import com.journeyflow.repositories.ChatRepository;
import com.journeyflow.repositories.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public ChatResponseDto createChat(ChatCreateRequestDto dto) {
        ChatEntity chat = new ChatEntity();
        applyFields(chat, dto.employeeId(), dto.sender(), dto.content(), dto.source(), dto.author(), dto.dateSent());
        return new ChatResponseDto(chatRepository.save(chat));
    }

    @Transactional
    public ChatResponseDto updateChat(Long id, ChatUpdateRequestDto dto) {
        ChatEntity chat = chatRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem nao encontrada"));
        applyFields(chat, dto.employeeId(), dto.sender(), dto.content(), dto.source(), dto.author(), dto.dateSent());
        return new ChatResponseDto(chatRepository.save(chat));
    }

    @Transactional
    public void deleteChat(Long id) {
        ChatEntity chat = chatRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem nao encontrada"));
        chatRepository.delete(chat);
    }

    @Transactional
    public List<ChatResponseDto> findAllChats() {
        return chatRepository.findAll().stream().map(ChatResponseDto::new).toList();
    }

    @Transactional
    public ChatResponseDto findChatById(Long id) {
        return chatRepository.findById(id)
                .map(ChatResponseDto::new)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem nao encontrada"));
    }

    // Valida os dados, busca o funcionario e preenche a mensagem.
    private void applyFields(ChatEntity chat, UUID employeeId, MessageSender sender, String content,
                             ContentSource source, String author, Instant dateSent) {
        if (employeeId == null || sender == null || content == null || author == null || dateSent == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campos obrigatorios da mensagem estao ausentes");
        }
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionario nao encontrado"));
        chat.setEmployee(employee);
        chat.setSender(sender);
        chat.setContent(content);
        chat.setSource(source == null ? ContentSource.TEXT : source);
        chat.setAuthor(author);
        chat.setDateSent(dateSent);
    }
}
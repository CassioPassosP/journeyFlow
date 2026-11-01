package com.journeyflow.controllers;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.journeyflow.dto.Chat.ChatCreateRequestDto;
import com.journeyflow.dto.Chat.ChatResponseDto;
import com.journeyflow.dto.Chat.ChatUpdateRequestDto;
import com.journeyflow.dto.Common.PageResponseDto;
import com.journeyflow.services.ChatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatResponseDto create(@Valid @RequestBody ChatCreateRequestDto request) {
        return chatService.createChat(request);
    }

    /** GET /chat?employeeId=...&page=0&size=20 (mais recentes primeiro). */
    @GetMapping
    public PageResponseDto<ChatResponseDto> findAll(
            @RequestParam(required = false) UUID employeeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return chatService.findChats(employeeId, pageable);
    }

    @GetMapping("/{id}")
    public ChatResponseDto findById(@PathVariable Long id) {
        return chatService.findChatById(id);
    }

    @PutMapping("/{id}")
    public ChatResponseDto update(@PathVariable Long id, @Valid @RequestBody ChatUpdateRequestDto request) {
        return chatService.updateChat(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        chatService.deleteChat(id);
    }
}
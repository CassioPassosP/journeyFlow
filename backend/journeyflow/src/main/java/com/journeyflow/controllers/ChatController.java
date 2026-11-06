package com.journeyflow.controllers;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
    public ChatResponseDto create(@Valid @RequestBody ChatCreateRequestDto request, Authentication authentication) {
        return chatService.createChat(request, authentication.getName());
    }

    @PostMapping("/quiz/{employeeId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatResponseDto startQuiz(@PathVariable UUID employeeId, Authentication authentication) {
        return chatService.startPillQuiz(employeeId, authentication.getName());
    }

    @PostMapping("/finish/{employeeId}")
    public ChatResponseDto finish(@PathVariable UUID employeeId, Authentication authentication) {
        return chatService.finishConversation(employeeId, authentication.getName());
    }

    /** GET /chat?employeeId=...&page=0&size=20; somente o histórico do próprio funcionário. */
    @GetMapping
    public PageResponseDto<ChatResponseDto> findAll(
            @RequestParam(required = false) UUID employeeId,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        return chatService.findChats(employeeId, pageable, authentication.getName());
    }

    @GetMapping("/{id}")
    public ChatResponseDto findById(@PathVariable Long id, Authentication authentication) {
        return chatService.findChatById(id, authentication.getName());
    }

    @PutMapping("/{id}")
    public ChatResponseDto update(@PathVariable Long id, @Valid @RequestBody ChatUpdateRequestDto request,
                                  Authentication authentication) {
        return chatService.updateChat(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        chatService.deleteChat(id, authentication.getName());
    }
}

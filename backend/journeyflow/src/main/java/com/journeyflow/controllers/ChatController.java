package com.journeyflow.controllers;

import com.journeyflow.dto.Alert.AlertCreateRequestDto;
import com.journeyflow.dto.Alert.AlertResponseDto;
import com.journeyflow.dto.Chat.ChatCreateRequestDto;
import com.journeyflow.dto.Chat.ChatResponseDto;
import com.journeyflow.services.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    //create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatResponseDto create(
            @Valid @RequestBody ChatCreateRequestDto request) {

        return chatService.createChat(request);
    }

    //find all
    @GetMapping
    public List<ChatResponseDto> findAll() {

        return chatService.findAllChats();
    }

    //find by id
    @GetMapping("/{id}")
    public ChatResponseDto findById(@PathVariable Long id) {

        return chatService.findChatById(id);
    }

    //update
    @PutMapping("/{id}")
    public ChatResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody ChatCreateRequestDto request) {

        return chatService.updateChat(id, request);
    }

    //delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        chatService.deleteChat(id);
    }
}

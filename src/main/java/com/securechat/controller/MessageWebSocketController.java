package com.securechat.controller;

import com.securechat.model.dto.ChatMessageDTO;
import com.securechat.service.MessageService;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Routes encrypted WebSocket payloads between authenticated users without decrypting them.
 * The controller trusts the authenticated principal for sender identity and only forwards
 * RSA/AES/SHA-256 artifacts that browsers can later decrypt and verify.
 */
@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO messageDTO, Principal principal) {
        messageDTO.setSenderUsername(principal.getName());
        messageDTO.setTimestamp(LocalDateTime.now().format(FORMATTER));
        if (messageDTO.getType() == null || messageDTO.getType().isBlank()) {
            messageDTO.setType("CHAT");
        }

        messageService.saveMessage(messageDTO);
        messagingTemplate.convertAndSendToUser(
                messageDTO.getReceiverUsername(),
                "/queue/messages",
                messageDTO
        );
        messagingTemplate.convertAndSendToUser(
                messageDTO.getSenderUsername(),
                "/queue/messages",
                messageDTO
        );
    }

    @MessageMapping("/chat.join")
    public void joinChat(@Payload ChatMessageDTO messageDTO, Principal principal) {
        messageDTO.setSenderUsername(principal.getName());
        messageDTO.setType("JOIN");
        messagingTemplate.convertAndSend("/topic/public", messageDTO);
    }
}

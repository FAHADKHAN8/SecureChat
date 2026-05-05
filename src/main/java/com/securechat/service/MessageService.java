package com.securechat.service;

import com.securechat.model.Message;
import com.securechat.model.dto.ChatMessageDTO;
import com.securechat.repository.MessageRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Persists and retrieves encrypted message records while keeping the server blind to plaintext.
 * The browser decrypts history later using its local private key and integrity hash checks.
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MessageRepository messageRepository;

    /**
     * Saves an already-encrypted chat DTO exactly as received because the backend never decrypts
     * or inspects sensitive message contents in a true E2EE workflow.
     */
    public Message saveMessage(ChatMessageDTO dto) {
        Message message = new Message();
        message.setSenderUsername(dto.getSenderUsername());
        message.setReceiverUsername(dto.getReceiverUsername());
        message.setEncryptedMessage(dto.getEncryptedMessage());
        message.setEncryptedAesKey(dto.getEncryptedAesKey());
        message.setIv(dto.getIv());
        message.setMessageHash(dto.getMessageHash());
        return messageRepository.save(message);
    }

    /**
     * Loads the full encrypted conversation between two users so each browser can decrypt the
     * messages it is able to read with its own private key.
     */
    public List<ChatMessageDTO> getConversation(String user1, String user2) {
        return messageRepository.findConversation(user1, user2)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Maps encrypted message entities to DTOs for WebSocket/history responses while preserving
     * ciphertext, IV, and hash data required by the browser decryption pipeline.
     */
    private ChatMessageDTO toDTO(Message message) {
        return new ChatMessageDTO(
                message.getSenderUsername(),
                message.getReceiverUsername(),
                message.getEncryptedMessage(),
                message.getEncryptedAesKey(),
                message.getIv(),
                message.getMessageHash(),
                message.getTimestamp() != null ? message.getTimestamp().format(FORMATTER) : "",
                "CHAT"
        );
    }
}

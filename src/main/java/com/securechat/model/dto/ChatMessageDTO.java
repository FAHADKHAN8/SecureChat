package com.securechat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transfers encrypted chat payloads between browser, WebSocket controller, and history API.
 * It contains AES ciphertext, the RSA-OAEP encrypted AES key, IV, and SHA-256 hash while
 * avoiding plaintext transfer through the server.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private String senderUsername;
    private String receiverUsername;
    private String encryptedMessage;
    private String encryptedAesKey;
    private String iv;
    private String messageHash;
    private String timestamp;
    private String type = "CHAT";
}

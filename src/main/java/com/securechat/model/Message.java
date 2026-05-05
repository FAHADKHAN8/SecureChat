package com.securechat.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persists encrypted chat payloads for history and delivery without exposing message contents.
 * The server stores ZERO plaintext: encryptedMessage is AES ciphertext, encryptedAesKey is the
 * RSA-wrapped session key, iv is the AES-CBC initialization vector, and messageHash is the
 * SHA-256 integrity value that clients verify after decryption.
 */
@Entity
@Table(
        name = "messages",
        indexes = {
                @Index(name = "idx_sender", columnList = "sender_username"),
                @Index(name = "idx_receiver", columnList = "receiver_username")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_username", nullable = false, length = 50)
    private String senderUsername;

    @Column(name = "receiver_username", nullable = false, length = 50)
    private String receiverUsername;

    @Column(name = "encrypted_message", nullable = false, columnDefinition = "LONGTEXT")
    private String encryptedMessage;

    @Column(name = "encrypted_aes_key", nullable = false, columnDefinition = "TEXT")
    private String encryptedAesKey;

    @Column(nullable = false, length = 255)
    private String iv;

    @Column(name = "message_hash", nullable = false, length = 64)
    private String messageHash;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Ensures every stored message has a timestamp for ordered conversation reconstruction.
     */
    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}

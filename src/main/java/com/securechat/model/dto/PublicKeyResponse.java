package com.securechat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Returns a recipient's public key so the sender browser can encrypt a fresh AES session key
 * with RSA-OAEP before sending ciphertext through the server.
 */
@Data
@AllArgsConstructor
public class PublicKeyResponse {
    private String username;
    private String publicKey;
}

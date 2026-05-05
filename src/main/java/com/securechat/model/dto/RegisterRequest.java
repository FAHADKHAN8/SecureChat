package com.securechat.model.dto;

import lombok.Data;

/**
 * Carries registration data from the browser, including the RSA public key generated locally
 * so the server can act as a public-key directory for end-to-end encrypted messaging.
 */
@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String publicKey;
}

package com.securechat.service;

import com.securechat.util.CryptoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Wraps low-level crypto helpers in a Spring-managed service so controllers or tests can verify
 * SHA-256 integrity without directly depending on utility construction details.
 */
@Service
@RequiredArgsConstructor
public class CryptoService {

    private final CryptoUtils cryptoUtils;

    /**
     * Verifies that a decrypted message matches the stored SHA-256 hash to detect tampering.
     */
    public boolean verifyMessageIntegrity(String decryptedMessage, String storedHash) {
        return cryptoUtils.verifySHA256(decryptedMessage, storedHash);
    }

    /**
     * Produces a SHA-256 hash for message integrity demonstrations and validation flows.
     */
    public String hashMessage(String message) {
        return cryptoUtils.computeSHA256(message);
    }
}

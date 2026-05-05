package com.securechat.util;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * Provides server-side cryptographic helpers for demonstration, validation, and academic
 * inspection even though the actual end-to-end encryption work is performed in the browser.
 * RSA-OAEP wraps AES keys, AES-CBC encrypts content, and SHA-256 verifies integrity.
 */
@Component
public class CryptoUtils {

    private static final String RSA_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int RSA_KEY_SIZE = 2048;
    private static final int AES_KEY_SIZE = 256;

    /**
     * Generates a fresh RSA-2048 key pair using SecureRandom for academic demonstrations of the
     * asymmetric step that browsers use to protect per-message AES keys.
     */
    public KeyPair generateRSAKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(RSA_KEY_SIZE, SecureRandom.getInstanceStrong());
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate RSA key pair", ex);
        }
    }

    /**
     * Encrypts arbitrary bytes with an RSA public key using OAEP and SHA-256, returning Base64
     * text so the result can be transmitted or stored alongside encrypted message metadata.
     */
    public String encryptWithRSA(byte[] data, String publicKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to encrypt data with RSA", ex);
        }
    }

    /**
     * Decrypts an RSA-OAEP Base64 ciphertext back into raw bytes, which is useful for recovering
     * an AES key during testing of the end-to-end message flow.
     */
    public byte[] decryptWithRSA(String encryptedBase64, PrivateKey pk) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, pk);
            return cipher.doFinal(Base64.getDecoder().decode(encryptedBase64));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to decrypt data with RSA", ex);
        }
    }

    /**
     * Generates a random 256-bit AES key for message confidentiality, mirroring the per-message
     * session key generation that occurs in the browser.
     */
    public SecretKey generateAESKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_SIZE, SecureRandom.getInstanceStrong());
            return keyGenerator.generateKey();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate AES key", ex);
        }
    }

    /**
     * Generates a random 16-byte IV required for AES-CBC so each encryption uses a unique
     * initialization vector and avoids deterministic ciphertext output.
     */
    public byte[] generateIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * Encrypts plaintext with AES-CBC and PKCS5 padding, returning Base64 ciphertext suitable for
     * persistence in the message table where the server still cannot read the original message.
     */
    public String encryptWithAES(String plaintext, SecretKey key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to encrypt data with AES", ex);
        }
    }

    /**
     * Decrypts AES-CBC Base64 ciphertext using raw AES key bytes and the provided Base64 IV so
     * demos can reconstruct plaintext and verify that integrity checks work as intended.
     */
    public String decryptWithAES(String ciphertextBase64, byte[] aesKeyBytes, String ivBase64) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, "AES");
            byte[] ivBytes = Base64.getDecoder().decode(ivBase64);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(ivBytes));
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(ciphertextBase64));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to decrypt data with AES", ex);
        }
    }

    /**
     * Computes a lowercase hexadecimal SHA-256 hash so encrypted messages can be checked for
     * tampering after the recipient decrypts them.
     */
    public String computeSHA256(String text) {
        try {
            byte[] digest = MessageDigest.getInstance(HASH_ALGORITHM)
                    .digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte value : digest) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to compute SHA-256 hash", ex);
        }
    }

    /**
     * Verifies a plaintext message against an expected SHA-256 hash using constant-time
     * comparison to reduce timing leakage during integrity validation.
     */
    public boolean verifySHA256(String plaintext, String expectedHash) {
        byte[] actual = computeSHA256(plaintext).getBytes(StandardCharsets.UTF_8);
        byte[] expected = expectedHash.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(actual, expected);
    }

    /**
     * Encodes a public RSA key to Base64 so it can be transported in JSON and persisted as the
     * discoverable half of the browser-generated key pair.
     */
    public String publicKeyToBase64(PublicKey pk) {
        return Base64.getEncoder().encodeToString(pk.getEncoded());
    }

    /**
     * Encodes a private RSA key to Base64 for testing and demonstration; production E2EE still
     * keeps this value in the browser rather than on the server.
     */
    public String privateKeyToBase64(PrivateKey pk) {
        return Base64.getEncoder().encodeToString(pk.getEncoded());
    }

    /**
     * Reconstructs a private RSA key from Base64 PKCS8 material so server-side tests can exercise
     * the full RSA decrypt path when validating the academic crypto flow.
     */
    public PrivateKey privateKeyFromBase64(String privateKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to reconstruct private key", ex);
        }
    }
}

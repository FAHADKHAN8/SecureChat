package com.securechat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Boots the SecureChat application and announces the runtime URL plus the academic
 * cryptography stack so it is obvious that message confidentiality is handled in the browser.
 */
@SpringBootApplication
public class E2EChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(E2EChatApplication.class, args);
        System.out.println("""
                 =========================================================
                  SecureChat is running at http://localhost:8080
                  Crypto stack: RSA-OAEP-2048 | AES-256-CBC | SHA-256 | BCrypt-12
                  End-to-end encryption happens in the browser only.
                 =========================================================
                """);
    }
}

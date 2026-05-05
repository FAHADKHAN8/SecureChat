# SecureChat

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![Security](https://img.shields.io/badge/E2EE-RSA%20%2B%20AES%20%2B%20SHA--256-orange)

SecureChat is an academic end-to-end encrypted chat demo built with Spring Boot, Thymeleaf, WebSocket/STOMP, MySQL, and the browser Web Crypto API. The server authenticates users, stores public keys, routes encrypted payloads, and persists ciphertext only. Plaintext and private keys stay in the browser.

## Crypto Stack

| Layer | Algorithm | Purpose |
|---|---|---|
| Asymmetric | RSA-OAEP 2048-bit + SHA-256 | Encrypt the per-message AES key |
| Symmetric | AES-256-CBC | Encrypt message content |
| Integrity | SHA-256 | Detect message tampering |
| Passwords | BCrypt strength 12 | Hash user passwords |

## Prerequisites

- Java 17
- Maven 3.9+
- MySQL 8.x

## Setup

1. Create the database:

```sql
CREATE DATABASE secure_chat_db;
```

2. Update `src/main/resources/application.properties` with your MySQL password.
3. Build the project:

```bash
mvn clean package
```

4. Run the application:

```bash
mvn spring-boot:run
```

5. Open `http://localhost:8080/register`.

## Test Flow

1. Register `alice` in one browser tab.
2. Register `bob` in an incognito window.
3. Login as both users and open the chat page.
4. Send a message from Alice to Bob.
5. Verify Bob can decrypt it and the integrity badge shows success.
6. Inspect MySQL:

```sql
SELECT encrypted_message, encrypted_aes_key, iv, message_hash FROM messages;
```

You should only see ciphertext and metadata, not plaintext.

## Project Structure

```text
src
├── main
│   ├── java/com/securechat
│   │   ├── controller
│   │   ├── model
│   │   ├── repository
│   │   ├── security
│   │   ├── service
│   │   ├── util
│   │   └── websocket
│   └── resources
│       ├── static
│       │   ├── css
│       │   └── js
│       └── templates
```

## CIA Triad

- Confidentiality: AES-256-CBC encrypts message content and RSA-OAEP encrypts the AES key.
- Integrity: SHA-256 is computed before encryption and verified after decryption.
- Availability: Spring Boot, WebSocket, and MySQL provide persistent real-time messaging.

## How E2EE Is Achieved

1. The browser generates an RSA-2048 key pair during registration.
2. The public key is sent to the server and stored in MySQL.
3. The private key stays only in `localStorage` on that browser.
4. Each outgoing message gets a fresh AES key and IV.
5. The browser encrypts the plaintext with AES-CBC.
6. The AES key is encrypted with the recipient's RSA public key.
7. The server stores and forwards only encrypted fields.
8. The recipient browser decrypts the AES key with its private key, decrypts the message, and verifies the SHA-256 hash.

## Known Limitations

- Private keys are stored in `localStorage`, which is acceptable for an academic demo but not production-grade.
- The sender does not re-decrypt its own sent plaintext because it encrypts with the recipient's public key.
- `setAllowedOriginPatterns("*")` is permissive for demo simplicity and should be tightened in production.

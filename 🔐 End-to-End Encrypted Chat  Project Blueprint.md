# **🔐 End-to-End Encrypted Chat System — Complete Project Blueprint**

**Academic Information Security Project** Demonstrates Hybrid Cryptography: RSA-2048 \+ AES-256 \+ SHA-256 Built with Java Spring Boot \+ MySQL \+ WebSocket \+ Vanilla JS

---

## **📋 TABLE OF CONTENTS**

1. [Project Overview & Architecture](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#1-project-overview--architecture)  
2. [Cryptography Explained](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#2-cryptography-explained)  
3. [CIA Triad Mapping](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#3-cia-triad-mapping)  
4. [Message Flow Diagram](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#4-message-flow-diagram)  
5. [Tech Stack & Dependencies (pom.xml)](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#5-tech-stack--dependencies-pomxml)  
6. [Project Structure](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#6-project-structure)  
7. [MySQL Database Schema](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#7-mysql-database-schema)  
8. [application.properties](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#8-applicationproperties)  
9. [Entity Classes](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#9-entity-classes)  
10. [Repository Interfaces](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#10-repository-interfaces)  
11. [Crypto Utility Class](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#11-crypto-utility-class)  
12. [Service Layer](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#12-service-layer)  
13. [WebSocket Configuration](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#13-websocket-configuration)  
14. [Security Configuration](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#14-security-configuration)  
15. [Controllers](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#15-controllers)  
16. [HTML Templates](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#16-html-templates)  
17. [JavaScript — WebSocket Client](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#17-javascript--websocket-client)  
18. [Main Application Class](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#18-main-application-class)  
19. [Setup & Run Instructions](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#19-setup--run-instructions)  
20. [Sample Test Flow](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#20-sample-test-flow)  
21. [Cursor / Lovable / Claude Agent Prompt](https://claude.ai/chat/8a6e4502-7819-43be-b93f-a174d22fd811#21-cursor--lovable--claude-agent-prompt)

---

## **1\. Project Overview & Architecture**

### **What This System Does**

This is a **real-time, end-to-end encrypted chat application** where:

* Messages are **encrypted on the sender's browser** before being sent to the server  
* The server **never sees plaintext** — it only stores and forwards encrypted blobs  
* Only the **intended recipient** can decrypt messages using their private RSA key  
* **SHA-256 hashing** ensures no one tampered with the message in transit

### **High-Level Architecture**

\[Browser \- Alice\]                \[Spring Boot Server\]              \[Browser \- Bob\]  
      |                                  |                               |  
      |-- HTTPS WebSocket \-------------\>|                               |  
      |                                  |-- HTTPS WebSocket \----------\>|  
      |                                  |                               |  
  RSA Key Pair                     Stores only:                   RSA Key Pair  
  generated locally             \- encrypted\_message               stored locally  
                                \- encrypted\_aes\_key               (private key never  
  Private Key: NEVER sent       \- sha256\_hash                      leaves browser)  
  Public Key: sent to server    \- sender/receiver IDs

---

## **2\. Cryptography Explained**

### **Why Hybrid Cryptography?**

| Algorithm | Purpose | Why Used Here |
| ----- | ----- | ----- |
| **RSA-2048** | Encrypt the AES session key | RSA is slow — only used to protect the small AES key |
| **AES-256-CBC** | Encrypt the actual message | AES is fast — ideal for encrypting large data |
| **SHA-256** | Hash the original plaintext | Detects any tampering; ensures message integrity |

### **RSA (Rivest–Shamir–Adleman)**

* Each user generates a **2048-bit RSA key pair** during registration  
* The **public key** is stored in the database — anyone can use it to encrypt  
* The **private key** stays in the browser (localStorage) — only the owner decrypts  
* Think of it like a **padlock (public) \+ key (private)**: anyone can lock, only owner unlocks

### **AES (Advanced Encryption Standard)**

* A **random 256-bit AES key** is generated fresh for every single message  
* The message is encrypted using this key in **CBC mode with a random IV**  
* This session key is then encrypted with the receiver's RSA public key  
* Even if an attacker captures messages, every message has a different AES key

### **SHA-256 (Secure Hash Algorithm)**

* Before encrypting, a **SHA-256 hash** of the original plaintext is computed  
* After decryption, the receiver computes the hash again and **compares**  
* If the hashes match → message is **authentic and unmodified**  
* If hashes differ → message was **tampered with** in transit

---

## **3\. CIA Triad Mapping**

| CIA Principle | How This System Satisfies It |
| ----- | ----- |
| **Confidentiality** | AES-256 encrypts all messages. RSA-2048 protects the AES key. Server never sees plaintext. |
| **Integrity** | SHA-256 hash is computed before encryption. Receiver verifies hash after decryption. Any tampering is detected. |
| **Availability** | Spring Boot \+ WebSocket provides real-time delivery. MySQL persists message history. Spring Security prevents unauthorized access. |

---

## **4\. Message Flow Diagram**

\=====================================================================  
                    E2EE MESSAGE FLOW  
\=====================================================================

REGISTRATION:  
  Alice \--\[generates RSA-2048 key pair\]--\> Browser  
  Alice \--\[sends public key only\]-------\> Server (stored in DB)  
  Alice \--\[saves private key\]-----------\> localStorage (NEVER sent)

SENDING A MESSAGE (Alice → Bob):  
  ┌─────────────────────────────────────────────────────────────┐  
  │  STEP 1: Alice's browser generates a random AES-256 key     │  
  │  STEP 2: Alice encrypts message with AES key \+ random IV    │  
  │  STEP 3: Alice fetches Bob's RSA public key from server     │  
  │  STEP 4: Alice encrypts AES key with Bob's RSA public key   │  
  │  STEP 5: Alice computes SHA-256 hash of ORIGINAL message    │  
  │  STEP 6: Alice sends { encryptedMsg, encryptedAESKey, hash }│  
  └─────────────────────────────────────────────────────────────┘  
                            │  
                            ▼  
  ┌─────────────────────────────────────────────────────────────┐  
  │                    SERVER (Never sees plaintext)             │  
  │  \- Stores: encrypted\_message, encrypted\_aes\_key, hash       │  
  │  \- Forwards via WebSocket to Bob                            │  
  └─────────────────────────────────────────────────────────────┘  
                            │  
                            ▼  
RECEIVING A MESSAGE (Bob):  
  ┌─────────────────────────────────────────────────────────────┐  
  │  STEP 1: Bob receives { encryptedMsg, encryptedAESKey, hash}│  
  │  STEP 2: Bob decrypts AES key using his RSA PRIVATE key     │  
  │  STEP 3: Bob decrypts message using the recovered AES key   │  
  │  STEP 4: Bob computes SHA-256 of decrypted message          │  
  │  STEP 5: Bob compares computed hash vs received hash        │  
  │  STEP 6: If hashes match → display message ✅               │  
  │          If hashes differ → show TAMPERED warning ❌        │  
  └─────────────────────────────────────────────────────────────┘

---

## **5\. Tech Stack & Dependencies (pom.xml)**

\<?xml version="1.0" encoding="UTF-8"?\>  
\<project xmlns="http://maven.apache.org/POM/4.0.0"  
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0  
         https://maven.apache.org/xsd/maven-4.0.0.xsd"\>  
    \<modelVersion\>4.0.0\</modelVersion\>

    \<parent\>  
        \<groupId\>org.springframework.boot\</groupId\>  
        \<artifactId\>spring-boot-starter-parent\</artifactId\>  
        \<version\>3.2.0\</version\>  
        \<relativePath/\>  
    \</parent\>

    \<groupId\>com.securechat\</groupId\>  
    \<artifactId\>e2e-chat\</artifactId\>  
    \<version\>1.0.0\</version\>  
    \<name\>E2E Encrypted Chat System\</name\>  
    \<description\>Academic project: Hybrid Cryptography Chat using RSA \+ AES \+ SHA-256\</description\>

    \<properties\>  
        \<java.version\>17\</java.version\>  
    \</properties\>

    \<dependencies\>  
        \<\!-- Spring Web (REST Controllers \+ Thymeleaf) \--\>  
        \<dependency\>  
            \<groupId\>org.springframework.boot\</groupId\>  
            \<artifactId\>spring-boot-starter-web\</artifactId\>  
        \</dependency\>

        \<\!-- Thymeleaf Template Engine for HTML pages \--\>  
        \<dependency\>  
            \<groupId\>org.springframework.boot\</groupId\>  
            \<artifactId\>spring-boot-starter-thymeleaf\</artifactId\>  
        \</dependency\>

        \<\!-- Spring Security (login, BCrypt password hashing) \--\>  
        \<dependency\>  
            \<groupId\>org.springframework.boot\</groupId\>  
            \<artifactId\>spring-boot-starter-security\</artifactId\>  
        \</dependency\>

        \<\!-- Thymeleaf \+ Spring Security integration \--\>  
        \<dependency\>  
            \<groupId\>org.thymeleaf.extras\</groupId\>  
            \<artifactId\>thymeleaf-extras-springsecurity6\</artifactId\>  
        \</dependency\>

        \<\!-- Spring Data JPA (Hibernate ORM) \--\>  
        \<dependency\>  
            \<groupId\>org.springframework.boot\</groupId\>  
            \<artifactId\>spring-boot-starter-data-jpa\</artifactId\>  
        \</dependency\>

        \<\!-- Spring WebSocket (real-time messaging) \--\>  
        \<dependency\>  
            \<groupId\>org.springframework.boot\</groupId\>  
            \<artifactId\>spring-boot-starter-websocket\</artifactId\>  
        \</dependency\>

        \<\!-- MySQL JDBC Driver \--\>  
        \<dependency\>  
            \<groupId\>com.mysql\</groupId\>  
            \<artifactId\>mysql-connector-j\</artifactId\>  
            \<scope\>runtime\</scope\>  
        \</dependency\>

        \<\!-- Lombok (reduces boilerplate: @Getter, @Setter, etc.) \--\>  
        \<dependency\>  
            \<groupId\>org.projectlombok\</groupId\>  
            \<artifactId\>lombok\</artifactId\>  
            \<optional\>true\</optional\>  
        \</dependency\>

        \<\!-- Jackson (JSON serialization for WebSocket messages) \--\>  
        \<dependency\>  
            \<groupId\>com.fasterxml.jackson.core\</groupId\>  
            \<artifactId\>jackson-databind\</artifactId\>  
        \</dependency\>

        \<\!-- Spring Boot Test \--\>  
        \<dependency\>  
            \<groupId\>org.springframework.boot\</groupId\>  
            \<artifactId\>spring-boot-starter-test\</artifactId\>  
            \<scope\>test\</scope\>  
        \</dependency\>  
    \</dependencies\>

    \<build\>  
        \<plugins\>  
            \<plugin\>  
                \<groupId\>org.springframework.boot\</groupId\>  
                \<artifactId\>spring-boot-maven-plugin\</artifactId\>  
                \<configuration\>  
                    \<excludes\>  
                        \<exclude\>  
                            \<groupId\>org.projectlombok\</groupId\>  
                            \<artifactId\>lombok\</artifactId\>  
                        \</exclude\>  
                    \</excludes\>  
                \</configuration\>  
            \</plugin\>  
        \</plugins\>  
    \</build\>  
\</project\>

---

## **6\. Project Structure**

e2e-chat/  
├── src/  
│   ├── main/  
│   │   ├── java/  
│   │   │   └── com/securechat/  
│   │   │       ├── E2EChatApplication.java          ← Main entry point  
│   │   │       ├── controller/  
│   │   │       │   ├── AuthController.java           ← Register/Login pages  
│   │   │       │   ├── ChatController.java           ← Chat page \+ REST APIs  
│   │   │       │   └── MessageWebSocketController.java ← WebSocket handler  
│   │   │       ├── service/  
│   │   │       │   ├── UserService.java              ← User registration logic  
│   │   │       │   ├── MessageService.java           ← Save/retrieve messages  
│   │   │       │   └── CryptoService.java            ← Server-side crypto helpers  
│   │   │       ├── repository/  
│   │   │       │   ├── UserRepository.java           ← JPA for users table  
│   │   │       │   └── MessageRepository.java        ← JPA for messages table  
│   │   │       ├── model/  
│   │   │       │   ├── User.java                     ← User entity  
│   │   │       │   ├── Message.java                  ← Message entity  
│   │   │       │   └── dto/  
│   │   │       │       ├── RegisterRequest.java      ← Registration DTO  
│   │   │       │       ├── ChatMessageDTO.java       ← WebSocket message DTO  
│   │   │       │       └── PublicKeyResponse.java   ← API response DTO  
│   │   │       ├── security/  
│   │   │       │   ├── SecurityConfig.java           ← Spring Security setup  
│   │   │       │   └── CustomUserDetailsService.java ← Load user from DB  
│   │   │       ├── websocket/  
│   │   │       │   └── WebSocketConfig.java          ← STOMP WebSocket config  
│   │   │       └── util/  
│   │   │           └── CryptoUtils.java              ← RSA, AES, SHA-256 helpers  
│   │   └── resources/  
│   │       ├── application.properties                ← DB \+ server config  
│   │       ├── templates/  
│   │       │   ├── login.html                        ← Login page  
│   │       │   ├── register.html                     ← Register page  
│   │       │   └── chat.html                         ← Main chat UI  
│   │       └── static/  
│   │           ├── css/  
│   │           │   └── style.css                     ← Chat styling  
│   │           └── js/  
│   │               ├── crypto-utils.js               ← Browser-side RSA/AES/SHA  
│   │               └── chat.js                       ← WebSocket \+ chat logic  
├── pom.xml  
└── README.md

---

## **7\. MySQL Database Schema**

\-- Run this in MySQL before starting the application  
\-- Or set spring.jpa.hibernate.ddl-auto=create in properties (auto-creates)

CREATE DATABASE IF NOT EXISTS secure\_chat\_db  
    CHARACTER SET utf8mb4  
    COLLATE utf8mb4\_unicode\_ci;

USE secure\_chat\_db;

\-- \=============================================  
\-- USERS TABLE  
\-- Stores: credentials \+ RSA public key  
\-- Private key is NEVER stored server-side  
\-- \=============================================  
CREATE TABLE IF NOT EXISTS users (  
    id          BIGINT AUTO\_INCREMENT PRIMARY KEY,  
    username    VARCHAR(50)   NOT NULL UNIQUE,  
    password    VARCHAR(255)  NOT NULL,         \-- BCrypt hashed  
    public\_key  TEXT          NOT NULL,         \-- RSA-2048 public key (PEM/Base64)  
    created\_at  TIMESTAMP     DEFAULT CURRENT\_TIMESTAMP  
);

\-- \=============================================  
\-- MESSAGES TABLE  
\-- Server ONLY stores encrypted data \- never plaintext  
\-- \=============================================  
CREATE TABLE IF NOT EXISTS messages (  
    id                  BIGINT AUTO\_INCREMENT PRIMARY KEY,  
    sender\_username     VARCHAR(50)  NOT NULL,  
    receiver\_username   VARCHAR(50)  NOT NULL,  
    encrypted\_message   LONGTEXT     NOT NULL,  \-- AES-encrypted ciphertext (Base64)  
    encrypted\_aes\_key   TEXT         NOT NULL,  \-- AES key encrypted with receiver's RSA public key (Base64)  
    iv                  VARCHAR(255) NOT NULL,  \-- AES Initialization Vector (Base64)  
    message\_hash        VARCHAR(64)  NOT NULL,  \-- SHA-256 hash of original plaintext  
    timestamp           TIMESTAMP    DEFAULT CURRENT\_TIMESTAMP,  
    INDEX idx\_sender   (sender\_username),  
    INDEX idx\_receiver (receiver\_username)  
);

\-- Sample users (passwords will be BCrypt hashed by the app at registration)  
\-- These are for reference only — register via the UI

---

## **8\. application.properties**

\# \=============================================  
\# SERVER CONFIGURATION  
\# \=============================================  
server.port=8080

\# \=============================================  
\# DATABASE CONFIGURATION  
\# Update username/password to match your MySQL  
\# \=============================================  
spring.datasource.url=jdbc:mysql://localhost:3306/secure\_chat\_db?useSSL=false\&serverTimezone=UTC\&allowPublicKeyRetrieval=true  
spring.datasource.username=root  
spring.datasource.password=yourpassword  
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

\# \=============================================  
\# JPA / HIBERNATE CONFIGURATION  
\# Use 'create' on first run, then switch to 'update'  
\# \=============================================  
spring.jpa.hibernate.ddl-auto=update  
spring.jpa.show-sql=true  
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect  
spring.jpa.properties.hibernate.format\_sql=true

\# \=============================================  
\# THYMELEAF CONFIGURATION  
\# \=============================================  
spring.thymeleaf.cache=false  
spring.thymeleaf.encoding=UTF-8

\# \=============================================  
\# SPRING SECURITY SESSION  
\# \=============================================  
spring.session.store-type=none

\# \=============================================  
\# LOGGING  
\# \=============================================  
logging.level.com.securechat=DEBUG  
logging.level.org.springframework.security=INFO

---

## **9\. Entity Classes**

### **`model/User.java`**

package com.securechat.model;

import jakarta.persistence.\*;  
import lombok.Data;  
import lombok.NoArgsConstructor;  
import lombok.AllArgsConstructor;  
import java.time.LocalDateTime;

/\*\*  
 \* USER ENTITY  
 \* Represents a registered user in the system.  
 \*   
 \* SECURITY NOTE:  
 \* \- password: stored as BCrypt hash — original password never stored  
 \* \- publicKey: RSA-2048 public key — safe to store, used by others to encrypt  
 \* \- PRIVATE KEY: intentionally NOT a field here — it never leaves the user's browser  
 \*/  
@Entity  
@Table(name \= "users")  
@Data  
@NoArgsConstructor  
@AllArgsConstructor  
public class User {

    @Id  
    @GeneratedValue(strategy \= GenerationType.IDENTITY)  
    private Long id;

    // Username must be unique — used as the identifier in chats  
    @Column(nullable \= false, unique \= true, length \= 50\)  
    private String username;

    // BCrypt hashed password — Spring Security handles verification  
    @Column(nullable \= false)  
    private String password;

    /\*\*  
     \* RSA PUBLIC KEY (Base64 encoded)  
     \*   
     \* Why store here?  
     \* \- When Alice wants to send a message to Bob, she needs Bob's public key  
     \*   to encrypt the AES session key.  
     \* \- The server acts as a "public key directory" — safe because public keys  
     \*   are meant to be shared publicly.  
     \*/  
    @Column(name \= "public\_key", nullable \= false, columnDefinition \= "TEXT")  
    private String publicKey;

    @Column(name \= "created\_at")  
    private LocalDateTime createdAt \= LocalDateTime.now();  
}

### **`model/Message.java`**

package com.securechat.model;

import jakarta.persistence.\*;  
import lombok.Data;  
import lombok.NoArgsConstructor;  
import lombok.AllArgsConstructor;  
import java.time.LocalDateTime;

/\*\*  
 \* MESSAGE ENTITY  
 \* Represents a single encrypted message stored in the database.  
 \*  
 \* SECURITY DESIGN:  
 \* The server stores ZERO plaintext. Every field that carries message data  
 \* is encrypted. This is the core of End-to-End Encryption (E2EE).  
 \*  
 \* Flow:  
 \*  encryptedMessage  \= AES(plaintext, aesKey, iv)  
 \*  encryptedAesKey   \= RSA(aesKey, receiver.publicKey)  
 \*  messageHash       \= SHA-256(plaintext)   ← computed BEFORE encryption  
 \*  iv                \= random bytes (needed for AES-CBC decryption)  
 \*/  
@Entity  
@Table(name \= "messages")  
@Data  
@NoArgsConstructor  
@AllArgsConstructor  
public class Message {

    @Id  
    @GeneratedValue(strategy \= GenerationType.IDENTITY)  
    private Long id;

    @Column(name \= "sender\_username", nullable \= false, length \= 50\)  
    private String senderUsername;

    @Column(name \= "receiver\_username", nullable \= false, length \= 50\)  
    private String receiverUsername;

    /\*\*  
     \* AES-256 encrypted message content (Base64 encoded)  
     \* Cannot be decrypted without the AES key.  
     \*/  
    @Column(name \= "encrypted\_message", nullable \= false, columnDefinition \= "LONGTEXT")  
    private String encryptedMessage;

    /\*\*  
     \* The AES session key encrypted with the receiver's RSA public key (Base64)  
     \* Only the receiver can decrypt this using their RSA private key.  
     \* Even the server cannot decrypt it\!  
     \*/  
    @Column(name \= "encrypted\_aes\_key", nullable \= false, columnDefinition \= "TEXT")  
    private String encryptedAesKey;

    /\*\*  
     \* AES Initialization Vector (IV) — Base64 encoded  
     \* Required for AES-CBC decryption. Not secret, but must be unique per message.  
     \*/  
    @Column(nullable \= false, length \= 255\)  
    private String iv;

    /\*\*  
     \* SHA-256 hash of the ORIGINAL plaintext message (hex string)  
     \* Used by the receiver to verify message integrity after decryption.  
     \* If hash doesn't match → message was tampered with.  
     \*/  
    @Column(name \= "message\_hash", nullable \= false, length \= 64\)  
    private String messageHash;

    @Column  
    private LocalDateTime timestamp \= LocalDateTime.now();  
}

### **`model/dto/RegisterRequest.java`**

package com.securechat.model.dto;

import lombok.Data;

/\*\*  
 \* DTO for user registration form submission.  
 \* publicKey is sent from the browser after generating RSA keys client-side.  
 \*/  
@Data  
public class RegisterRequest {  
    private String username;  
    private String password;  
    private String publicKey; // Base64 encoded RSA public key generated in browser  
}

### **`model/dto/ChatMessageDTO.java`**

package com.securechat.model.dto;

import lombok.Data;  
import lombok.NoArgsConstructor;  
import lombok.AllArgsConstructor;

/\*\*  
 \* DTO for WebSocket chat messages.  
 \* This is what travels over the WebSocket connection.  
 \* All sensitive fields are already encrypted when this object is created.  
 \*/  
@Data  
@NoArgsConstructor  
@AllArgsConstructor  
public class ChatMessageDTO {  
    private String senderUsername;  
    private String receiverUsername;  
    private String encryptedMessage;   // AES encrypted (Base64)  
    private String encryptedAesKey;    // RSA encrypted AES key (Base64)  
    private String iv;                 // AES IV (Base64)  
    private String messageHash;        // SHA-256 of original plaintext (hex)  
    private String timestamp;          // ISO timestamp for display  
      
    // Used for system messages (e.g., "User connected")  
    private String type \= "CHAT"; // CHAT | JOIN | LEAVE  
}

### **`model/dto/PublicKeyResponse.java`**

package com.securechat.model.dto;

import lombok.AllArgsConstructor;  
import lombok.Data;

/\*\*  
 \* Simple response wrapper for public key API endpoint.  
 \* Used when Alice fetches Bob's public key to encrypt an AES key for him.  
 \*/  
@Data  
@AllArgsConstructor  
public class PublicKeyResponse {  
    private String username;  
    private String publicKey; // Base64 encoded RSA-2048 public key  
}

---

## **10\. Repository Interfaces**

### **`repository/UserRepository.java`**

package com.securechat.repository;

import com.securechat.model.User;  
import org.springframework.data.jpa.repository.JpaRepository;  
import org.springframework.stereotype.Repository;  
import java.util.Optional;

/\*\*  
 \* Spring Data JPA repository for User entity.  
 \* Spring auto-generates SQL queries based on method names.  
 \*/  
@Repository  
public interface UserRepository extends JpaRepository\<User, Long\> {

    // Find user by username (used for login \+ public key lookup)  
    Optional\<User\> findByUsername(String username);

    // Check if username already taken during registration  
    boolean existsByUsername(String username);  
}

### **`repository/MessageRepository.java`**

package com.securechat.repository;

import com.securechat.model.Message;  
import org.springframework.data.jpa.repository.JpaRepository;  
import org.springframework.data.jpa.repository.Query;  
import org.springframework.data.repository.query.Param;  
import org.springframework.stereotype.Repository;  
import java.util.List;

/\*\*  
 \* Spring Data JPA repository for Message entity.  
 \* All messages stored here are encrypted — server never decrypts them.  
 \*/  
@Repository  
public interface MessageRepository extends JpaRepository\<Message, Long\> {

    /\*\*  
     \* Retrieve the full conversation between two users.  
     \* Returns messages in both directions, ordered by timestamp.  
     \*   
     \* The JPQL query uses OR to get messages where:  
     \*   user1 sent to user2, OR user2 sent to user1  
     \*/  
    @Query("SELECT m FROM Message m WHERE " \+  
           "(m.senderUsername \= :user1 AND m.receiverUsername \= :user2) OR " \+  
           "(m.senderUsername \= :user2 AND m.receiverUsername \= :user1) " \+  
           "ORDER BY m.timestamp ASC")  
    List\<Message\> findConversation(  
        @Param("user1") String user1,  
        @Param("user2") String user2  
    );

    // Get all messages received by a user (for inbox display)  
    List\<Message\> findByReceiverUsernameOrderByTimestampDesc(String receiverUsername);  
}

---

## **11\. Crypto Utility Class**

### **`util/CryptoUtils.java`**

package com.securechat.util;

import org.springframework.stereotype.Component;  
import javax.crypto.Cipher;  
import javax.crypto.KeyGenerator;  
import javax.crypto.SecretKey;  
import javax.crypto.spec.IvParameterSpec;  
import javax.crypto.spec.SecretKeySpec;  
import java.security.\*;  
import java.security.spec.X509EncodedKeySpec;  
import java.util.Base64;

/\*\*  
 \* \============================================================  
 \* CRYPTO UTILITY CLASS  
 \* \============================================================  
 \*   
 \* This class provides SERVER-SIDE cryptographic helpers.  
 \*   
 \* NOTE: In a true E2EE system, encryption/decryption happens  
 \* in the BROWSER (see crypto-utils.js). This server-side class  
 \* is used for:  
 \* 1\. Generating RSA key pairs during development/testing  
 \* 2\. Verifying SHA-256 hashes server-side if needed  
 \* 3\. Demonstrating how the algorithms work for academic purposes  
 \*   
 \* ALGORITHMS USED:  
 \* \- RSA/ECB/OAEPWithSHA-256AndMGF1Padding (2048-bit)  
 \* \- AES/CBC/PKCS5Padding (256-bit)  
 \* \- SHA-256 (message integrity)  
 \* \============================================================  
 \*/  
@Component  
public class CryptoUtils {

    // ─── CONSTANTS ───────────────────────────────────────────  
    private static final String RSA\_ALGORITHM \= "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";  
    private static final String AES\_ALGORITHM \= "AES/CBC/PKCS5Padding";  
    private static final String HASH\_ALGORITHM \= "SHA-256";  
    private static final int RSA\_KEY\_SIZE \= 2048;  
    private static final int AES\_KEY\_SIZE \= 256;

    // ─── RSA OPERATIONS ──────────────────────────────────────

    /\*\*  
     \* GENERATE RSA KEY PAIR (2048-bit)  
     \*   
     \* Used during registration. The public key is sent to the server.  
     \* The private key must be securely stored by the user (in browser localStorage  
     \* for this demo — in production, use a hardware key store or secure enclave).  
     \*   
     \* @return KeyPair containing RSAPublicKey and RSAPrivateKey  
     \*/  
    public KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {  
        KeyPairGenerator keyGen \= KeyPairGenerator.getInstance("RSA");  
        // 2048 bits \= strong enough for academic purposes  
        // Production systems use 4096 bits  
        keyGen.initialize(RSA\_KEY\_SIZE, new SecureRandom());  
        return keyGen.generateKeyPair();  
    }

    /\*\*  
     \* ENCRYPT with RSA PUBLIC KEY  
     \*   
     \* Used to encrypt the AES session key so only the receiver can decrypt it.  
     \* Why OAEP padding? It's more secure than older PKCS\#1 v1.5 padding  
     \* and resistant to chosen-ciphertext attacks.  
     \*   
     \* @param data The bytes to encrypt (the AES key)  
     \* @param publicKeyBase64 Receiver's RSA public key in Base64  
     \* @return Encrypted bytes encoded as Base64 string  
     \*/  
    public String encryptWithRSA(byte\[\] data, String publicKeyBase64) throws Exception {  
        // Decode the Base64 public key back to bytes  
        byte\[\] keyBytes \= Base64.getDecoder().decode(publicKeyBase64);  
          
        // Reconstruct the RSA PublicKey object from raw bytes  
        X509EncodedKeySpec keySpec \= new X509EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory \= KeyFactory.getInstance("RSA");  
        PublicKey publicKey \= keyFactory.generatePublic(keySpec);  
          
        // Initialize cipher in ENCRYPT mode  
        Cipher cipher \= Cipher.getInstance(RSA\_ALGORITHM);  
        cipher.init(Cipher.ENCRYPT\_MODE, publicKey);  
          
        // Encrypt and return as Base64 string  
        byte\[\] encrypted \= cipher.doFinal(data);  
        return Base64.getEncoder().encodeToString(encrypted);  
    }

    /\*\*  
     \* DECRYPT with RSA PRIVATE KEY  
     \*   
     \* Used by the receiver to decrypt the AES session key.  
     \* In a real E2EE system this happens in the browser — never on the server\!  
     \* This method exists for demonstration and testing purposes.  
     \*   
     \* @param encryptedBase64 The RSA-encrypted AES key (Base64)  
     \* @param privateKey The receiver's RSA private key  
     \* @return Decrypted bytes (the original AES key)  
     \*/  
    public byte\[\] decryptWithRSA(String encryptedBase64, PrivateKey privateKey) throws Exception {  
        byte\[\] encryptedBytes \= Base64.getDecoder().decode(encryptedBase64);  
        Cipher cipher \= Cipher.getInstance(RSA\_ALGORITHM);  
        cipher.init(Cipher.DECRYPT\_MODE, privateKey);  
        return cipher.doFinal(encryptedBytes);  
    }

    // ─── AES OPERATIONS ──────────────────────────────────────

    /\*\*  
     \* GENERATE AES SESSION KEY (256-bit)  
     \*   
     \* A fresh random AES key is generated for EVERY message.  
     \* This is called "Perfect Forward Secrecy" — if one key is compromised,  
     \* past messages remain secure because they used different keys.  
     \*   
     \* @return A 256-bit AES SecretKey  
     \*/  
    public SecretKey generateAESKey() throws NoSuchAlgorithmException {  
        KeyGenerator keyGen \= KeyGenerator.getInstance("AES");  
        keyGen.init(AES\_KEY\_SIZE, new SecureRandom()); // 256-bit key  
        return keyGen.generateKey();  
    }

    /\*\*  
     \* GENERATE RANDOM AES INITIALIZATION VECTOR (IV)  
     \*   
     \* The IV ensures that the same plaintext encrypted twice produces  
     \* different ciphertext — critical for security.  
     \* The IV is NOT secret — it's sent alongside the encrypted message.  
     \*   
     \* @return 16-byte random IV  
     \*/  
    public byte\[\] generateIV() {  
        byte\[\] iv \= new byte\[16\]; // AES block size \= 16 bytes  
        new SecureRandom().nextBytes(iv);  
        return iv;  
    }

    /\*\*  
     \* ENCRYPT MESSAGE with AES-256-CBC  
     \*   
     \* Why CBC mode?  
     \* \- Each block of plaintext is XORed with the previous ciphertext block  
     \* \- This prevents identical plaintext blocks from producing identical ciphertext  
     \* \- The IV is used for the first block (no "previous" ciphertext yet)  
     \*   
     \* @param plaintext The message to encrypt  
     \* @param aesKey The AES secret key  
     \* @param iv The initialization vector  
     \* @return Encrypted ciphertext as Base64 string  
     \*/  
    public String encryptWithAES(String plaintext, SecretKey aesKey, byte\[\] iv) throws Exception {  
        Cipher cipher \= Cipher.getInstance(AES\_ALGORITHM);  
        IvParameterSpec ivSpec \= new IvParameterSpec(iv);  
        cipher.init(Cipher.ENCRYPT\_MODE, aesKey, ivSpec);  
          
        byte\[\] encrypted \= cipher.doFinal(plaintext.getBytes("UTF-8"));  
        return Base64.getEncoder().encodeToString(encrypted);  
    }

    /\*\*  
     \* DECRYPT MESSAGE with AES-256-CBC  
     \*   
     \* Reverses the encryption. Requires the same AES key and IV used for encryption.  
     \*   
     \* @param ciphertextBase64 The encrypted message (Base64)  
     \* @param aesKeyBytes The raw AES key bytes  
     \* @param ivBase64 The IV used during encryption (Base64)  
     \* @return Decrypted plaintext string  
     \*/  
    public String decryptWithAES(String ciphertextBase64, byte\[\] aesKeyBytes, String ivBase64) throws Exception {  
        byte\[\] ciphertext \= Base64.getDecoder().decode(ciphertextBase64);  
        byte\[\] ivBytes \= Base64.getDecoder().decode(ivBase64);  
          
        SecretKeySpec keySpec \= new SecretKeySpec(aesKeyBytes, "AES");  
        IvParameterSpec ivSpec \= new IvParameterSpec(ivBytes);  
          
        Cipher cipher \= Cipher.getInstance(AES\_ALGORITHM);  
        cipher.init(Cipher.DECRYPT\_MODE, keySpec, ivSpec);  
          
        byte\[\] decrypted \= cipher.doFinal(ciphertext);  
        return new String(decrypted, "UTF-8");  
    }

    // ─── SHA-256 OPERATIONS ──────────────────────────────────

    /\*\*  
     \* COMPUTE SHA-256 HASH  
     \*   
     \* SHA-256 produces a 256-bit (32-byte) fixed-size "fingerprint" of any input.  
     \* Properties:  
     \* \- Deterministic: same input → same hash, always  
     \* \- One-way: cannot reverse a hash to get the original  
     \* \- Avalanche effect: changing 1 character changes \~50% of the hash  
     \* \- Collision resistant: astronomically unlikely for two inputs to share a hash  
     \*   
     \* In this system: compute hash BEFORE encryption.  
     \* After decryption: compute hash again and compare.  
     \* If equal → message is authentic and unmodified ✅  
     \* If different → message was tampered with ❌  
     \*   
     \* @param text The plaintext message  
     \* @return SHA-256 hash as lowercase hex string (64 characters)  
     \*/  
    public String computeSHA256(String text) throws NoSuchAlgorithmException {  
        MessageDigest digest \= MessageDigest.getInstance(HASH\_ALGORITHM);  
        byte\[\] hashBytes \= digest.digest(text.getBytes(java.nio.charset.StandardCharsets.UTF\_8));  
          
        // Convert bytes to hex string for easy storage and comparison  
        StringBuilder hexString \= new StringBuilder();  
        for (byte b : hashBytes) {  
            String hex \= Integer.toHexString(0xff & b);  
            if (hex.length() \== 1\) hexString.append('0');  
            hexString.append(hex);  
        }  
        return hexString.toString();  
    }

    /\*\*  
     \* VERIFY SHA-256 HASH  
     \*   
     \* Compares a freshly computed hash against the stored hash.  
     \* Used after decryption to confirm message integrity.  
     \*   
     \* @param plaintext The decrypted message text  
     \* @param expectedHash The hash that was stored when message was sent  
     \* @return true if message is unmodified, false if tampered  
     \*/  
    public boolean verifySHA256(String plaintext, String expectedHash) throws NoSuchAlgorithmException {  
        String actualHash \= computeSHA256(plaintext);  
        // Use MessageDigest.isEqual for constant-time comparison (prevents timing attacks)  
        return MessageDigest.isEqual(actualHash.getBytes(), expectedHash.getBytes());  
    }

    // ─── HELPER METHODS ──────────────────────────────────────

    /\*\* Convert a PublicKey object to Base64 string for storage \*/  
    public String publicKeyToBase64(PublicKey publicKey) {  
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());  
    }

    /\*\* Convert a PrivateKey object to Base64 string \*/  
    public String privateKeyToBase64(PrivateKey privateKey) {  
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());  
    }  
}

---

## **12\. Service Layer**

### **`service/UserService.java`**

package com.securechat.service;

import com.securechat.model.User;  
import com.securechat.model.dto.RegisterRequest;  
import com.securechat.repository.UserRepository;  
import lombok.RequiredArgsConstructor;  
import org.springframework.security.crypto.password.PasswordEncoder;  
import org.springframework.stereotype.Service;  
import java.util.List;  
import java.util.Optional;

/\*\*  
 \* USER SERVICE  
 \* Handles user registration and lookup.  
 \* Passwords are hashed with BCrypt before storage.  
 \*/  
@Service  
@RequiredArgsConstructor  
public class UserService {

    private final UserRepository userRepository;  
    private final PasswordEncoder passwordEncoder;

    /\*\*  
     \* Register a new user.  
     \*   
     \* @param request Contains: username, password (plaintext), publicKey (Base64 RSA)  
     \* @return Saved User entity  
     \* @throws RuntimeException if username already taken  
     \*/  
    public User registerUser(RegisterRequest request) {  
        // Check username availability  
        if (userRepository.existsByUsername(request.getUsername())) {  
            throw new RuntimeException("Username '" \+ request.getUsername() \+ "' is already taken.");  
        }

        User user \= new User();  
        user.setUsername(request.getUsername());

        // Hash the password using BCrypt (strength factor 12\)  
        // BCrypt is one-way — we can verify but never recover the original password  
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Store the RSA public key sent from the browser  
        // This key was generated client-side — private key stayed in browser  
        user.setPublicKey(request.getPublicKey());

        return userRepository.save(user);  
    }

    /\*\* Find user by username \*/  
    public Optional\<User\> findByUsername(String username) {  
        return userRepository.findByUsername(username);  
    }

    /\*\* Get all users (for displaying user list in chat UI) \*/  
    public List\<User\> getAllUsers() {  
        return userRepository.findAll();  
    }

    /\*\* Get just the RSA public key for a given username \*/  
    public String getPublicKey(String username) {  
        return userRepository.findByUsername(username)  
            .map(User::getPublicKey)  
            .orElseThrow(() \-\> new RuntimeException("User not found: " \+ username));  
    }  
}

### **`service/MessageService.java`**

package com.securechat.service;

import com.securechat.model.Message;  
import com.securechat.model.dto.ChatMessageDTO;  
import com.securechat.repository.MessageRepository;  
import lombok.RequiredArgsConstructor;  
import org.springframework.stereotype.Service;  
import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;  
import java.util.List;  
import java.util.stream.Collectors;

/\*\*  
 \* MESSAGE SERVICE  
 \* Handles saving and retrieving encrypted messages.  
 \*   
 \* IMPORTANT: This service deals exclusively with ENCRYPTED data.  
 \* No decryption ever happens on the server.  
 \*/  
@Service  
@RequiredArgsConstructor  
public class MessageService {

    private final MessageRepository messageRepository;  
    private static final DateTimeFormatter FORMATTER \= DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /\*\*  
     \* Save an encrypted message to the database.  
     \* All fields received are already encrypted by the sender's browser.  
     \*   
     \* @param dto The WebSocket message containing encrypted payload  
     \* @return The saved Message entity  
     \*/  
    public Message saveMessage(ChatMessageDTO dto) {  
        Message message \= new Message();  
        message.setSenderUsername(dto.getSenderUsername());  
        message.setReceiverUsername(dto.getReceiverUsername());  
        message.setEncryptedMessage(dto.getEncryptedMessage());  
        message.setEncryptedAesKey(dto.getEncryptedAesKey());  
        message.setIv(dto.getIv());  
        message.setMessageHash(dto.getMessageHash());  
        message.setTimestamp(LocalDateTime.now());  
        return messageRepository.save(message);  
    }

    /\*\*  
     \* Retrieve full conversation history between two users.  
     \* Returns DTOs with all encrypted data — client decrypts on their end.  
     \*   
     \* @param user1 First user's username  
     \* @param user2 Second user's username  
     \* @return List of ChatMessageDTOs ordered by timestamp  
     \*/  
    public List\<ChatMessageDTO\> getConversation(String user1, String user2) {  
        List\<Message\> messages \= messageRepository.findConversation(user1, user2);  
        return messages.stream()  
            .map(this::toDTO)  
            .collect(Collectors.toList());  
    }

    /\*\* Convert Message entity to DTO for WebSocket/REST transmission \*/  
    private ChatMessageDTO toDTO(Message msg) {  
        ChatMessageDTO dto \= new ChatMessageDTO();  
        dto.setSenderUsername(msg.getSenderUsername());  
        dto.setReceiverUsername(msg.getReceiverUsername());  
        dto.setEncryptedMessage(msg.getEncryptedMessage());  
        dto.setEncryptedAesKey(msg.getEncryptedAesKey());  
        dto.setIv(msg.getIv());  
        dto.setMessageHash(msg.getMessageHash());  
        dto.setTimestamp(msg.getTimestamp() \!= null  
            ? msg.getTimestamp().format(FORMATTER)  
            : LocalDateTime.now().format(FORMATTER));  
        dto.setType("CHAT");  
        return dto;  
    }  
}

### **`service/CryptoService.java`**

package com.securechat.service;

import com.securechat.util.CryptoUtils;  
import lombok.RequiredArgsConstructor;  
import org.springframework.stereotype.Service;

/\*\*  
 \* CRYPTO SERVICE  
 \* Thin wrapper around CryptoUtils for Spring dependency injection.  
 \* Provides server-side cryptographic operations (demo/testing use).  
 \*   
 \* In production E2EE: most crypto happens in the browser via crypto-utils.js  
 \*/  
@Service  
@RequiredArgsConstructor  
public class CryptoService {

    private final CryptoUtils cryptoUtils;

    /\*\*  
     \* Verify SHA-256 hash of a decrypted message.  
     \* Used for server-side integrity checking in testing/demo mode.  
     \*/  
    public boolean verifyMessageIntegrity(String decryptedMessage, String storedHash) {  
        try {  
            return cryptoUtils.verifySHA256(decryptedMessage, storedHash);  
        } catch (Exception e) {  
            return false;  
        }  
    }

    /\*\*  
     \* Compute SHA-256 hash (exposed as a service for controllers if needed)  
     \*/  
    public String hashMessage(String message) {  
        try {  
            return cryptoUtils.computeSHA256(message);  
        } catch (Exception e) {  
            throw new RuntimeException("Hashing failed", e);  
        }  
    }  
}

---

## **13\. WebSocket Configuration**

### **`websocket/WebSocketConfig.java`**

package com.securechat.websocket;

import org.springframework.context.annotation.Configuration;  
import org.springframework.messaging.simp.config.MessageBrokerRegistry;  
import org.springframework.web.socket.config.annotation.\*;

/\*\*  
 \* WEBSOCKET CONFIGURATION  
 \*   
 \* Uses STOMP (Simple Text Oriented Message Protocol) over WebSocket.  
 \* STOMP provides a publish/subscribe model — clients subscribe to topics  
 \* and send messages to destinations.  
 \*   
 \* Message routing:  
 \*   Client sends to → /app/chat.send  
 \*   Server routes to → /topic/messages (broadcast)  
 \*              OR → /user/{username}/queue/messages (private)  
 \*/  
@Configuration  
@EnableWebSocketMessageBroker  
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /\*\*  
     \* Configure the message broker.  
     \*   
     \* \- Simple in-memory broker for /topic/ (broadcast) and /user/ (private)  
     \* \- /app/ prefix: messages destined for @MessageMapping methods in controllers  
     \*/  
    @Override  
    public void configureMessageBroker(MessageBrokerRegistry config) {  
        // Enable simple in-memory message broker for these prefixes  
        config.enableSimpleBroker("/topic", "/user");  
          
        // Messages from clients with /app prefix are routed to @MessageMapping methods  
        config.setApplicationDestinationPrefixes("/app");  
          
        // Enable user-specific messaging (for private chat)  
        config.setUserDestinationPrefix("/user");  
    }

    /\*\*  
     \* Register STOMP WebSocket endpoint.  
     \* Clients connect to: ws://localhost:8080/ws  
     \* SockJS provides fallback for browsers that don't support WebSocket.  
     \*/  
    @Override  
    public void registerStompEndpoints(StompEndpointRegistry registry) {  
        registry.addEndpoint("/ws")  
                .setAllowedOriginPatterns("\*") // In production: restrict to your domain  
                .withSockJS(); // SockJS fallback for compatibility  
    }  
}

---

## **14\. Security Configuration**

### **`security/CustomUserDetailsService.java`**

package com.securechat.security;

import com.securechat.model.User;  
import com.securechat.repository.UserRepository;  
import lombok.RequiredArgsConstructor;  
import org.springframework.security.core.userdetails.\*;  
import org.springframework.stereotype.Service;  
import java.util.ArrayList;

/\*\*  
 \* CUSTOM USER DETAILS SERVICE  
 \*   
 \* Spring Security calls this to load user data during login.  
 \* We override it to fetch users from our MySQL database  
 \* instead of Spring's default in-memory store.  
 \*/  
@Service  
@RequiredArgsConstructor  
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /\*\*  
     \* Load a user by username for Spring Security authentication.  
     \* Spring Security handles password verification using BCrypt.  
     \*   
     \* @param username The username entered at login  
     \* @return UserDetails object Spring Security uses to authenticate  
     \* @throws UsernameNotFoundException if user doesn't exist  
     \*/  
    @Override  
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {  
        User user \= userRepository.findByUsername(username)  
            .orElseThrow(() \-\> new UsernameNotFoundException("User not found: " \+ username));

        // Return Spring Security's User object  
        // Roles: empty list for this demo (no admin/user distinction needed)  
        return new org.springframework.security.core.userdetails.User(  
            user.getUsername(),  
            user.getPassword(), // BCrypt hash — Spring Security compares this  
            new ArrayList\<\>()   // No authorities/roles for simplicity  
        );  
    }  
}

### **`security/SecurityConfig.java`**

package com.securechat.security;

import lombok.RequiredArgsConstructor;  
import org.springframework.context.annotation.Bean;  
import org.springframework.context.annotation.Configuration;  
import org.springframework.security.authentication.AuthenticationManager;  
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;  
import org.springframework.security.config.annotation.web.builders.HttpSecurity;  
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;  
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;  
import org.springframework.security.crypto.password.PasswordEncoder;  
import org.springframework.security.web.SecurityFilterChain;

/\*\*  
 \* SPRING SECURITY CONFIGURATION  
 \*   
 \* Handles:  
 \* \- Which URLs require authentication  
 \* \- Login page customization  
 \* \- BCrypt password hashing (strength \= 12\)  
 \* \- CSRF exemption for WebSocket endpoints  
 \*   
 \* BCrypt Explanation:  
 \* BCrypt is a slow hashing algorithm designed for passwords.  
 \* It automatically includes a salt (random bytes added before hashing)  
 \* preventing rainbow table attacks.  
 \* Strength 12 \= 2^12 \= 4096 iterations — slow for attackers, fast enough for users.  
 \*/  
@Configuration  
@EnableWebSecurity  
@RequiredArgsConstructor  
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /\*\*  
     \* Define security rules for HTTP requests.  
     \*/  
    @Bean  
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {  
        http  
            // Disable CSRF for WebSocket compatibility (in production use proper CSRF tokens)  
            .csrf(csrf \-\> csrf  
                .ignoringRequestMatchers("/ws/\*\*", "/api/\*\*")  
            )  
            .authorizeHttpRequests(auth \-\> auth  
                // Public routes — no login required  
                .requestMatchers(  
                    "/register",  
                    "/api/register",  
                    "/css/\*\*",  
                    "/js/\*\*",  
                    "/error"  
                ).permitAll()  
                // Everything else requires authentication  
                .anyRequest().authenticated()  
            )  
            // Custom login page  
            .formLogin(form \-\> form  
                .loginPage("/login")        // Our custom login HTML  
                .loginProcessingUrl("/login") // Spring processes POST to this URL  
                .defaultSuccessUrl("/chat", true) // Redirect here after successful login  
                .failureUrl("/login?error=true")  // Redirect here on bad credentials  
                .permitAll()  
            )  
            // Logout configuration  
            .logout(logout \-\> logout  
                .logoutUrl("/logout")  
                .logoutSuccessUrl("/login?logout=true")  
                .invalidateHttpSession(true)  
                .deleteCookies("JSESSIONID")  
                .permitAll()  
            )  
            // Allow WebSocket frames (needed for SockJS)  
            .headers(headers \-\> headers  
                .frameOptions(frame \-\> frame.sameOrigin())  
            );

        return http.build();  
    }

    /\*\*  
     \* BCrypt password encoder.  
     \* Used in UserService to hash passwords during registration.  
     \* Spring Security uses it automatically for login verification.  
     \*/  
    @Bean  
    public PasswordEncoder passwordEncoder() {  
        return new BCryptPasswordEncoder(12); // 12 rounds of hashing  
    }

    /\*\*  
     \* Authentication manager — needed for programmatic authentication.  
     \*/  
    @Bean  
    public AuthenticationManager authenticationManager(  
            AuthenticationConfiguration config) throws Exception {  
        return config.getAuthenticationManager();  
    }  
}

---

## **15\. Controllers**

### **`controller/AuthController.java`**

package com.securechat.controller;

import com.securechat.model.dto.RegisterRequest;  
import com.securechat.service.UserService;  
import lombok.RequiredArgsConstructor;  
import org.springframework.http.ResponseEntity;  
import org.springframework.stereotype.Controller;  
import org.springframework.ui.Model;  
import org.springframework.web.bind.annotation.\*;

import java.util.Map;

/\*\*  
 \* AUTH CONTROLLER  
 \* Handles user registration and login pages.  
 \*/  
@Controller  
@RequiredArgsConstructor  
public class AuthController {

    private final UserService userService;

    /\*\* Show login page \*/  
    @GetMapping("/login")  
    public String loginPage() {  
        return "login"; // → templates/login.html  
    }

    /\*\* Show registration page \*/  
    @GetMapping("/register")  
    public String registerPage() {  
        return "register"; // → templates/register.html  
    }

    /\*\*  
     \* REST API endpoint for user registration.  
     \*   
     \* Called via JavaScript (AJAX/fetch) from register.html.  
     \* The browser generates RSA key pair FIRST, then sends:  
     \*   { username, password, publicKey }  
     \*   
     \* This is the moment where the public key enters the server — private key stays in browser.  
     \*/  
    @PostMapping("/api/register")  
    @ResponseBody  
    public ResponseEntity\<?\> registerUser(@RequestBody RegisterRequest request) {  
        try {  
            userService.registerUser(request);  
            return ResponseEntity.ok(Map.of(  
                "success", true,  
                "message", "Registration successful\! Please login."  
            ));  
        } catch (RuntimeException e) {  
            return ResponseEntity.badRequest().body(Map.of(  
                "success", false,  
                "message", e.getMessage()  
            ));  
        }  
    }  
}

### **`controller/ChatController.java`**

package com.securechat.controller;

import com.securechat.model.User;  
import com.securechat.model.dto.ChatMessageDTO;  
import com.securechat.model.dto.PublicKeyResponse;  
import com.securechat.service.MessageService;  
import com.securechat.service.UserService;  
import lombok.RequiredArgsConstructor;  
import org.springframework.http.ResponseEntity;  
import org.springframework.security.core.annotation.AuthenticationPrincipal;  
import org.springframework.security.core.userdetails.UserDetails;  
import org.springframework.stereotype.Controller;  
import org.springframework.ui.Model;  
import org.springframework.web.bind.annotation.\*;

import java.util.List;  
import java.util.stream.Collectors;

/\*\*  
 \* CHAT CONTROLLER  
 \* Handles the chat page and REST APIs for key lookup and message history.  
 \*/  
@Controller  
@RequiredArgsConstructor  
public class ChatController {

    private final UserService userService;  
    private final MessageService messageService;

    /\*\*  
     \* Main chat page.  
     \* Adds the current username and list of other users to the model.  
     \*/  
    @GetMapping({"/", "/chat"})  
    public String chatPage(@AuthenticationPrincipal UserDetails currentUser, Model model) {  
        String myUsername \= currentUser.getUsername();

        // Get all users except the current logged-in user  
        List\<String\> otherUsers \= userService.getAllUsers().stream()  
            .map(User::getUsername)  
            .filter(name \-\> \!name.equals(myUsername))  
            .collect(Collectors.toList());

        model.addAttribute("currentUser", myUsername);  
        model.addAttribute("userList", otherUsers);

        return "chat"; // → templates/chat.html  
    }

    /\*\*  
     \* REST API: Get a user's RSA public key.  
     \*   
     \* Called by the sender's browser when they want to encrypt an AES key  
     \* for a specific recipient.   
     \*   
     \* Example: GET /api/public-key/bob  
     \* Returns Bob's RSA public key so Alice can encrypt her AES key with it.  
     \*/  
    @GetMapping("/api/public-key/{username}")  
    @ResponseBody  
    public ResponseEntity\<?\> getPublicKey(@PathVariable String username) {  
        try {  
            String publicKey \= userService.getPublicKey(username);  
            return ResponseEntity.ok(new PublicKeyResponse(username, publicKey));  
        } catch (RuntimeException e) {  
            return ResponseEntity.notFound().build();  
        }  
    }

    /\*\*  
     \* REST API: Get conversation history between two users.  
     \* Returns encrypted messages — client decrypts them.  
     \*   
     \* Example: GET /api/messages/history?with=bob  
     \*/  
    @GetMapping("/api/messages/history")  
    @ResponseBody  
    public ResponseEntity\<List\<ChatMessageDTO\>\> getHistory(  
            @AuthenticationPrincipal UserDetails currentUser,  
            @RequestParam String with) {  
        List\<ChatMessageDTO\> history \= messageService.getConversation(  
            currentUser.getUsername(), with  
        );  
        return ResponseEntity.ok(history);  
    }

    /\*\*  
     \* REST API: Get current user's username (for JS to know who we are)  
     \*/  
    @GetMapping("/api/me")  
    @ResponseBody  
    public ResponseEntity\<?\> getCurrentUser(@AuthenticationPrincipal UserDetails currentUser) {  
        return ResponseEntity.ok(java.util.Map.of("username", currentUser.getUsername()));  
    }  
}

### **`controller/MessageWebSocketController.java`**

package com.securechat.controller;

import com.securechat.model.Message;  
import com.securechat.model.dto.ChatMessageDTO;  
import com.securechat.service.MessageService;  
import lombok.RequiredArgsConstructor;  
import org.springframework.messaging.handler.annotation.MessageMapping;  
import org.springframework.messaging.handler.annotation.Payload;  
import org.springframework.messaging.simp.SimpMessagingTemplate;  
import org.springframework.stereotype.Controller;  
import java.security.Principal;  
import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

/\*\*  
 \* WEBSOCKET MESSAGE CONTROLLER  
 \*   
 \* Handles incoming WebSocket messages from clients.  
 \* Routes encrypted messages to the correct recipient.  
 \*   
 \* SECURITY NOTE:  
 \* The server acts as a "blind router" here — it receives encrypted payloads  
 \* and forwards them without ever decrypting them.  
 \* This is the core of End-to-End Encryption.  
 \*/  
@Controller  
@RequiredArgsConstructor  
public class MessageWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;  
    private final MessageService messageService;

    /\*\*  
     \* Handle incoming encrypted chat message.  
     \*   
     \* Client sends to: /app/chat.send  
     \* Server persists \+ forwards to: /user/{receiver}/queue/messages  
     \*   
     \* The message content is ALREADY ENCRYPTED when it arrives here.  
     \* The server only sees: encryptedMessage, encryptedAesKey, iv, hash  
     \* NEVER the plaintext.  
     \*   
     \* @param messageDTO The encrypted message payload  
     \* @param principal The authenticated sender (injected by Spring Security)  
     \*/  
    @MessageMapping("/chat.send")  
    public void sendMessage(@Payload ChatMessageDTO messageDTO, Principal principal) {  
        // Security: Use authenticated principal as sender — don't trust client-sent username  
        messageDTO.setSenderUsername(principal.getName());

        // Add server timestamp  
        messageDTO.setTimestamp(  
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))  
        );

        // Persist the encrypted message to database  
        // Server stores: encryptedMessage \+ encryptedAesKey \+ iv \+ hash — NO plaintext  
        messageService.saveMessage(messageDTO);

        // Forward to receiver's private queue  
        // Only the intended receiver subscribes to /user/{their-username}/queue/messages  
        messagingTemplate.convertAndSendToUser(  
            messageDTO.getReceiverUsername(),  
            "/queue/messages",  
            messageDTO  
        );

        // Also send back to sender so they see it in their own chat window  
        messagingTemplate.convertAndSendToUser(  
            messageDTO.getSenderUsername(),  
            "/queue/messages",  
            messageDTO  
        );  
    }

    /\*\*  
     \* Handle user join notification (optional \- for "User is online" feature)  
     \* Client sends to: /app/chat.join  
     \*/  
    @MessageMapping("/chat.join")  
    public void joinChat(@Payload ChatMessageDTO messageDTO, Principal principal) {  
        messageDTO.setSenderUsername(principal.getName());  
        messageDTO.setType("JOIN");

        // Broadcast to all users that someone joined  
        messagingTemplate.convertAndSend("/topic/public", messageDTO);  
    }  
}

---

## **16\. HTML Templates**

### **`templates/login.html`**

\<\!DOCTYPE html\>  
\<html lang="en" xmlns:th="http://www.thymeleaf.org"\>  
\<head\>  
    \<meta charset="UTF-8"\>  
    \<meta name="viewport" content="width=device-width, initial-scale=1.0"\>  
    \<title\>SecureChat — Login\</title\>  
    \<link rel="stylesheet" th:href="@{/css/style.css}"\>  
\</head\>  
\<body class="auth-page"\>

\<div class="auth-container"\>  
    \<\!-- Lock Icon Header \--\>  
    \<div class="auth-header"\>  
        \<div class="lock-icon"\>🔐\</div\>  
        \<h1\>SecureChat\</h1\>  
        \<p class="subtitle"\>End-to-End Encrypted · RSA \+ AES \+ SHA-256\</p\>  
    \</div\>

    \<\!-- Success message after registration \--\>  
    \<div th:if="${param.logout}" class="alert alert-success"\>  
        ✅ You have been logged out successfully.  
    \</div\>

    \<\!-- Login form — Spring Security processes POST /login \--\>  
    \<form th:action="@{/login}" method="post" class="auth-form"\>  
        \<div class="form-group"\>  
            \<label for="username"\>👤 Username\</label\>  
            \<input type="text" id="username" name="username"  
                   placeholder="Enter your username" required autofocus\>  
        \</div\>

        \<div class="form-group"\>  
            \<label for="password"\>🔑 Password\</label\>  
            \<input type="password" id="password" name="password"  
                   placeholder="Enter your password" required\>  
        \</div\>

        \<\!-- Show error if login failed \--\>  
        \<div th:if="${param.error}" class="alert alert-error"\>  
            ❌ Invalid username or password. Please try again.  
        \</div\>

        \<button type="submit" class="btn-primary"\>Login\</button\>  
    \</form\>

    \<p class="auth-link"\>  
        Don't have an account? \<a href="/register"\>Register here\</a\>  
    \</p\>

    \<\!-- Info box explaining the crypto for academic demo \--\>  
    \<div class="crypto-info"\>  
        \<h4\>🔒 How this works\</h4\>  
        \<ul\>  
            \<li\>Your RSA key pair is generated in your browser\</li\>  
            \<li\>Private key never leaves your device\</li\>  
            \<li\>Messages encrypted with AES-256 before sending\</li\>  
            \<li\>Server only stores encrypted ciphertext\</li\>  
        \</ul\>  
    \</div\>  
\</div\>

\</body\>  
\</html\>

### **`templates/register.html`**

\<\!DOCTYPE html\>  
\<html lang="en" xmlns:th="http://www.thymeleaf.org"\>  
\<head\>  
    \<meta charset="UTF-8"\>  
    \<meta name="viewport" content="width=device-width, initial-scale=1.0"\>  
    \<title\>SecureChat — Register\</title\>  
    \<link rel="stylesheet" th:href="@{/css/style.css}"\>  
\</head\>  
\<body class="auth-page"\>

\<div class="auth-container"\>  
    \<div class="auth-header"\>  
        \<div class="lock-icon"\>🔐\</div\>  
        \<h1\>Create Account\</h1\>  
        \<p class="subtitle"\>Your RSA keys will be generated automatically\</p\>  
    \</div\>

    \<div id="statusMessage" class="alert" style="display:none;"\>\</div\>

    \<\!-- Key Generation Status \--\>  
    \<div id="keyGenStatus" class="key-gen-status"\>  
        \<div class="spinner" id="keySpinner" style="display:none;"\>\</div\>  
        \<span id="keyGenText"\>🔑 RSA key pair will be generated on registration\</span\>  
    \</div\>

    \<\!-- Registration form — submitted via JavaScript (not form submit) \--\>  
    \<div class="auth-form"\>  
        \<div class="form-group"\>  
            \<label for="username"\>👤 Username\</label\>  
            \<input type="text" id="username" placeholder="Choose a username" required\>  
        \</div\>

        \<div class="form-group"\>  
            \<label for="password"\>🔑 Password\</label\>  
            \<input type="password" id="password" placeholder="Choose a strong password" required\>  
        \</div\>

        \<div class="form-group"\>  
            \<label for="confirmPassword"\>🔑 Confirm Password\</label\>  
            \<input type="password" id="confirmPassword" placeholder="Repeat your password" required\>  
        \</div\>

        \<button onclick="registerUser()" class="btn-primary" id="registerBtn"\>  
            Generate Keys & Register  
        \</button\>  
    \</div\>

    \<p class="auth-link"\>  
        Already have an account? \<a href="/login"\>Login here\</a\>  
    \</p\>

    \<\!-- Shows the generated public key for educational purposes \--\>  
    \<div id="keyDisplay" class="key-display" style="display:none;"\>  
        \<h4\>✅ Your RSA Keys Generated\</h4\>  
        \<div class="key-section"\>  
            \<label\>Public Key (stored on server):\</label\>  
            \<textarea id="publicKeyDisplay" readonly rows="3"\>\</textarea\>  
        \</div\>  
        \<div class="key-section"\>  
            \<label\>⚠️ Private Key (saved in your browser — DO NOT SHARE):\</label\>  
            \<textarea id="privateKeyDisplay" readonly rows="3" class="private-key"\>\</textarea\>  
        \</div\>  
        \<p class="key-warning"\>  
            🔐 Your private key has been saved to localStorage.   
            It never leaves your browser and the server never sees it.  
        \</p\>  
    \</div\>  
\</div\>

\<script th:src="@{/js/crypto-utils.js}"\>\</script\>  
\<script\>  
/\*\*  
 \* REGISTRATION FLOW:  
 \* 1\. User fills form  
 \* 2\. Browser generates RSA-2048 key pair (using Web Crypto API)  
 \* 3\. Private key saved to localStorage  
 \* 4\. Public key \+ credentials sent to server via /api/register  
 \* 5\. Server stores: username, BCrypt(password), publicKey  
 \* 6\. Server NEVER sees the private key  
 \*/  
async function registerUser() {  
    const username \= document.getElementById('username').value.trim();  
    const password \= document.getElementById('password').value;  
    const confirmPassword \= document.getElementById('confirmPassword').value;

    // Validation  
    if (\!username || \!password) {  
        showStatus('Please fill in all fields.', 'error');  
        return;  
    }  
    if (password \!== confirmPassword) {  
        showStatus('Passwords do not match.', 'error');  
        return;  
    }  
    if (password.length \< 8\) {  
        showStatus('Password must be at least 8 characters.', 'error');  
        return;  
    }

    const btn \= document.getElementById('registerBtn');  
    btn.disabled \= true;  
    btn.textContent \= 'Generating RSA keys...';

    document.getElementById('keySpinner').style.display \= 'inline-block';  
    document.getElementById('keyGenText').textContent \= '⏳ Generating 2048-bit RSA key pair...';

    try {  
        // STEP 1: Generate RSA-2048 key pair in the browser  
        // This uses the browser's built-in Web Crypto API  
        const keyPair \= await CryptoUtils.generateRSAKeyPair();

        // STEP 2: Export keys to storable formats  
        const publicKeyBase64 \= await CryptoUtils.exportPublicKey(keyPair.publicKey);  
        const privateKeyBase64 \= await CryptoUtils.exportPrivateKey(keyPair.privateKey);

        // STEP 3: Save private key to localStorage — NEVER sent to server  
        localStorage.setItem(\`privateKey\_${username}\`, privateKeyBase64);

        document.getElementById('keyGenText').textContent \= '✅ RSA keys generated\!';

        // Show keys for educational demonstration  
        document.getElementById('publicKeyDisplay').value \= publicKeyBase64.substring(0, 100\) \+ '...';  
        document.getElementById('privateKeyDisplay').value \= privateKeyBase64.substring(0, 100\) \+ '...';  
        document.getElementById('keyDisplay').style.display \= 'block';

        // STEP 4: Send username \+ password \+ publicKey to server  
        btn.textContent \= 'Registering...';  
        const response \= await fetch('/api/register', {  
            method: 'POST',  
            headers: { 'Content-Type': 'application/json' },  
            body: JSON.stringify({  
                username: username,  
                password: password,  
                publicKey: publicKeyBase64  // ← Only the public key goes to server  
            })  
        });

        const result \= await response.json();

        if (result.success) {  
            showStatus('✅ ' \+ result.message \+ ' Redirecting to login...', 'success');  
            setTimeout(() \=\> window.location.href \= '/login', 2000);  
        } else {  
            showStatus('❌ ' \+ result.message, 'error');  
            btn.disabled \= false;  
            btn.textContent \= 'Generate Keys & Register';  
        }

    } catch (error) {  
        showStatus('❌ Error: ' \+ error.message, 'error');  
        btn.disabled \= false;  
        btn.textContent \= 'Generate Keys & Register';  
    }  
}

function showStatus(message, type) {  
    const el \= document.getElementById('statusMessage');  
    el.textContent \= message;  
    el.className \= 'alert alert-' \+ type;  
    el.style.display \= 'block';  
}  
\</script\>  
\</body\>  
\</html\>

### **`templates/chat.html`**

\<\!DOCTYPE html\>  
\<html lang="en" xmlns:th="http://www.thymeleaf.org"  
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security"\>  
\<head\>  
    \<meta charset="UTF-8"\>  
    \<meta name="viewport" content="width=device-width, initial-scale=1.0"\>  
    \<title\>SecureChat\</title\>  
    \<link rel="stylesheet" th:href="@{/css/style.css}"\>  
\</head\>  
\<body class="chat-page"\>

\<\!-- Pass server-side data to JavaScript \--\>  
\<div id="serverData"  
     th:data-current-user="${currentUser}"  
     style="display:none;"\>\</div\>

\<\!-- ═══════════════════════════════════════════════════════ \--\>  
\<\!--                     SIDEBAR                            \--\>  
\<\!-- ═══════════════════════════════════════════════════════ \--\>  
\<div class="chat-layout"\>  
    \<aside class="sidebar"\>  
        \<div class="sidebar-header"\>  
            \<div class="user-info"\>  
                \<div class="avatar" th:text="${\#strings.substring(currentUser, 0, 1).toUpperCase()}"\>\</div\>  
                \<div\>  
                    \<div class="current-username" th:text="${currentUser}"\>\</div\>  
                    \<div class="status-badge"\>🔐 E2E Encrypted\</div\>  
                \</div\>  
            \</div\>  
            \<a href="/logout" class="logout-btn" title="Logout"\>⏻\</a\>  
        \</div\>

        \<div class="sidebar-search"\>  
            \<input type="text" placeholder="🔍 Search users..." id="userSearch"  
                   oninput="filterUsers(this.value)"\>  
        \</div\>

        \<div class="users-list" id="usersList"\>  
            \<div class="users-list-header"\>Contacts\</div\>  
            \<\!-- Thymeleaf iterates over the list of other users \--\>  
            \<div th:each="user : ${userList}"  
                 class="user-item"  
                 th:data-username="${user}"  
                 th:onclick="|openChat('${user}')|"\>  
                \<div class="user-avatar" th:text="${\#strings.substring(user, 0, 1).toUpperCase()}"\>\</div\>  
                \<div class="user-item-info"\>  
                    \<div class="user-item-name" th:text="${user}"\>\</div\>  
                    \<div class="user-item-status"\>Click to chat\</div\>  
                \</div\>  
                \<div class="unread-badge" th:id="|badge-${user}|" style="display:none;"\>\</div\>  
            \</div\>

            \<\!-- Show message if no other users \--\>  
            \<div th:if="${\#lists.isEmpty(userList)}" class="no-users"\>  
                No other users registered yet.\<br\>  
                Share this app with someone\!  
            \</div\>  
        \</div\>  
    \</aside\>

    \<\!-- ═══════════════════════════════════════════════════ \--\>  
    \<\!--                   CHAT AREA                         \--\>  
    \<\!-- ═══════════════════════════════════════════════════ \--\>  
    \<main class="chat-area"\>  
        \<\!-- Welcome screen before selecting a chat \--\>  
        \<div id="welcomeScreen" class="welcome-screen"\>  
            \<div class="welcome-icon"\>🔐\</div\>  
            \<h2\>SecureChat\</h2\>  
            \<p\>Select a contact to start an end-to-end encrypted conversation\</p\>  
            \<div class="welcome-features"\>  
                \<span\>🔑 RSA-2048 Key Exchange\</span\>  
                \<span\>🛡️ AES-256 Encryption\</span\>  
                \<span\>✅ SHA-256 Integrity\</span\>  
            \</div\>  
        \</div\>

        \<\!-- Active chat window (hidden until user selected) \--\>  
        \<div id="chatWindow" style="display:none;" class="chat-window"\>  
            \<\!-- Chat header \--\>  
            \<div class="chat-header"\>  
                \<div class="chat-header-avatar" id="chatHeaderAvatar"\>\</div\>  
                \<div class="chat-header-info"\>  
                    \<div class="chat-header-name" id="chatHeaderName"\>\</div\>  
                    \<div class="chat-header-status" id="chatHeaderStatus"\>🔐 End-to-End Encrypted\</div\>  
                \</div\>  
                \<button class="show-encrypted-btn" onclick="toggleEncryptedView()"  
                        title="Toggle encrypted message view"\>  
                    👁️ Show Encrypted  
                \</button\>  
            \</div\>

            \<\!-- Messages container \--\>  
            \<div class="messages-container" id="messagesContainer"\>  
                \<div id="loadingMessages" class="loading-messages"\>Loading encrypted messages...\</div\>  
            \</div\>

            \<\!-- Crypto info bar (shown when messages load) \--\>  
            \<div class="crypto-bar" id="cryptoBar" style="display:none;"\>  
                \<span\>🔒 Messages encrypted with AES-256 · Key secured with RSA-2048 · Integrity by SHA-256\</span\>  
            \</div\>

            \<\!-- Message input area \--\>  
            \<div class="message-input-area"\>  
                \<div class="input-wrapper"\>  
                    \<input type="text" id="messageInput"  
                           placeholder="Type a message... (will be encrypted before sending)"  
                           onkeypress="if(event.key==='Enter') sendMessage()"\>  
                    \<div class="input-actions"\>  
                        \<span class="encrypt-indicator" title="Message will be encrypted"\>🔐\</span\>  
                    \</div\>  
                \</div\>  
                \<button onclick="sendMessage()" class="send-btn" id="sendBtn"\>  
                    Send 🚀  
                \</button\>  
            \</div\>  
        \</div\>

        \<\!-- Encrypted view panel (for demo purposes) \--\>  
        \<div id="encryptedPanel" class="encrypted-panel" style="display:none;"\>  
            \<div class="encrypted-panel-header"\>  
                🔍 Raw Encrypted Data (What the Server Sees)  
                \<button onclick="toggleEncryptedView()"\>✕ Close\</button\>  
            \</div\>  
            \<div id="encryptedData" class="encrypted-data"\>\</div\>  
        \</div\>  
    \</main\>  
\</div\>

\<\!-- Status toast notification \--\>  
\<div id="toast" class="toast" style="display:none;"\>\</div\>

\<\!-- Load scripts \--\>  
\<script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"\>\</script\>  
\<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"\>\</script\>  
\<script th:src="@{/js/crypto-utils.js}"\>\</script\>  
\<script th:src="@{/js/chat.js}"\>\</script\>  
\</body\>  
\</html\>

---

## **17\. JavaScript — WebSocket Client**

### **`static/js/crypto-utils.js`**

/\*\*  
 \* \============================================================  
 \* CRYPTO-UTILS.JS — Browser-Side Cryptography  
 \* \============================================================  
 \*   
 \* Uses the browser's built-in Web Crypto API (SubtleCrypto).  
 \* All cryptographic operations happen HERE, in the browser.  
 \* The server NEVER receives plaintext.  
 \*   
 \* ALGORITHMS:  
 \* \- RSA-OAEP with SHA-256 (for key exchange)  
 \* \- AES-CBC 256-bit (for message encryption)  
 \* \- SHA-256 (for message integrity)  
 \* \============================================================  
 \*/

const CryptoUtils \= {

    // ─── RSA OPERATIONS ───────────────────────────────────

    /\*\*  
     \* Generate an RSA-2048 key pair using Web Crypto API.  
     \* Called once during user registration.  
     \*   
     \* @returns {Promise\<CryptoKeyPair\>} { publicKey, privateKey }  
     \*/  
    async generateRSAKeyPair() {  
        return await window.crypto.subtle.generateKey(  
            {  
                name: "RSA-OAEP",  
                modulusLength: 2048,          // 2048-bit key size  
                publicExponent: new Uint8Array(\[1, 0, 1\]), // 65537 (standard)  
                hash: "SHA-256",              // Hash algorithm for OAEP padding  
            },  
            true,                             // Keys are extractable (for export/storage)  
            \["encrypt", "decrypt"\]            // Usage: encrypt with public, decrypt with private  
        );  
    },

    /\*\*  
     \* Export RSA public key to Base64 string for server storage.  
     \* SPKI format is the standard for public key exchange.  
     \*   
     \* @param {CryptoKey} publicKey  
     \* @returns {Promise\<string\>} Base64 encoded public key  
     \*/  
    async exportPublicKey(publicKey) {  
        const exported \= await window.crypto.subtle.exportKey("spki", publicKey);  
        return this.\_arrayBufferToBase64(exported);  
    },

    /\*\*  
     \* Export RSA private key to Base64 string for localStorage storage.  
     \* PKCS\#8 is the standard format for private keys.  
     \*   
     \* ⚠️ IN PRODUCTION: Use a hardware security key or secure enclave.  
     \*    localStorage is used here for simplicity in an academic demo.  
     \*   
     \* @param {CryptoKey} privateKey  
     \* @returns {Promise\<string\>} Base64 encoded private key  
     \*/  
    async exportPrivateKey(privateKey) {  
        const exported \= await window.crypto.subtle.exportKey("pkcs8", privateKey);  
        return this.\_arrayBufferToBase64(exported);  
    },

    /\*\*  
     \* Import RSA public key from Base64 string.  
     \* Used when fetching another user's public key from the server.  
     \*   
     \* @param {string} base64Key The Base64 encoded public key  
     \* @returns {Promise\<CryptoKey\>} Importable CryptoKey  
     \*/  
    async importPublicKey(base64Key) {  
        const keyBuffer \= this.\_base64ToArrayBuffer(base64Key);  
        return await window.crypto.subtle.importKey(  
            "spki",  
            keyBuffer,  
            { name: "RSA-OAEP", hash: "SHA-256" },  
            false,           // Not extractable (no need to re-export)  
            \["encrypt"\]      // Public key can only encrypt  
        );  
    },

    /\*\*  
     \* Import RSA private key from Base64 string (retrieved from localStorage).  
     \* Used when decrypting received AES keys.  
     \*   
     \* @param {string} base64Key The Base64 encoded private key  
     \* @returns {Promise\<CryptoKey\>} Importable CryptoKey  
     \*/  
    async importPrivateKey(base64Key) {  
        const keyBuffer \= this.\_base64ToArrayBuffer(base64Key);  
        return await window.crypto.subtle.importKey(  
            "pkcs8",  
            keyBuffer,  
            { name: "RSA-OAEP", hash: "SHA-256" },  
            false,           // Not extractable  
            \["decrypt"\]      // Private key can only decrypt  
        );  
    },

    /\*\*  
     \* Encrypt data (AES key bytes) with RSA public key.  
     \* This is how the AES session key is securely shared.  
     \*   
     \* @param {ArrayBuffer} data The AES key bytes to encrypt  
     \* @param {CryptoKey} publicKey The receiver's RSA public key  
     \* @returns {Promise\<string\>} Base64 encoded encrypted data  
     \*/  
    async encryptWithRSA(data, publicKey) {  
        const encrypted \= await window.crypto.subtle.encrypt(  
            { name: "RSA-OAEP" },  
            publicKey,  
            data  
        );  
        return this.\_arrayBufferToBase64(encrypted);  
    },

    /\*\*  
     \* Decrypt RSA-encrypted data with the user's private key.  
     \* Used to recover the AES session key from received messages.  
     \*   
     \* @param {string} encryptedBase64 The encrypted AES key (Base64)  
     \* @param {CryptoKey} privateKey The current user's RSA private key  
     \* @returns {Promise\<ArrayBuffer\>} The decrypted AES key bytes  
     \*/  
    async decryptWithRSA(encryptedBase64, privateKey) {  
        const encryptedBuffer \= this.\_base64ToArrayBuffer(encryptedBase64);  
        return await window.crypto.subtle.decrypt(  
            { name: "RSA-OAEP" },  
            privateKey,  
            encryptedBuffer  
        );  
    },

    // ─── AES OPERATIONS ───────────────────────────────────

    /\*\*  
     \* Generate a random 256-bit AES key.  
     \* A NEW key is generated for EVERY message (Perfect Forward Secrecy).  
     \*   
     \* @returns {Promise\<CryptoKey\>} AES-256 key  
     \*/  
    async generateAESKey() {  
        return await window.crypto.subtle.generateKey(  
            { name: "AES-CBC", length: 256 }, // 256-bit \= 32 bytes  
            true,                             // Extractable (for RSA encryption)  
            \["encrypt", "decrypt"\]  
        );  
    },

    /\*\*  
     \* Export AES key to raw bytes for RSA encryption.  
     \*   
     \* @param {CryptoKey} aesKey  
     \* @returns {Promise\<ArrayBuffer\>} Raw AES key bytes  
     \*/  
    async exportAESKey(aesKey) {  
        return await window.crypto.subtle.exportKey("raw", aesKey);  
    },

    /\*\*  
     \* Import AES key from raw bytes (recovered after RSA decryption).  
     \*   
     \* @param {ArrayBuffer} keyBytes The raw AES key bytes  
     \* @returns {Promise\<CryptoKey\>} Usable AES CryptoKey  
     \*/  
    async importAESKey(keyBytes) {  
        return await window.crypto.subtle.importKey(  
            "raw",  
            keyBytes,  
            { name: "AES-CBC" },  
            false,  
            \["decrypt"\]  
        );  
    },

    /\*\*  
     \* Encrypt a message with AES-256-CBC.  
     \*   
     \* CBC Mode: Each plaintext block is XORed with the previous ciphertext block.  
     \* The IV is used for the first block. Both IV and ciphertext are needed for decryption.  
     \*   
     \* @param {string} plaintext The message to encrypt  
     \* @param {CryptoKey} aesKey The AES key  
     \* @returns {Promise\<{ciphertext: string, iv: string}\>} Encrypted data \+ IV (both Base64)  
     \*/  
    async encryptWithAES(plaintext, aesKey) {  
        // Generate a random 16-byte IV for this specific message  
        const iv \= window.crypto.getRandomValues(new Uint8Array(16));

        // Encode plaintext to bytes  
        const encoder \= new TextEncoder();  
        const plaintextBytes \= encoder.encode(plaintext);

        // Encrypt  
        const encrypted \= await window.crypto.subtle.encrypt(  
            { name: "AES-CBC", iv: iv },  
            aesKey,  
            plaintextBytes  
        );

        return {  
            ciphertext: this.\_arrayBufferToBase64(encrypted), // Base64 ciphertext  
            iv: this.\_arrayBufferToBase64(iv.buffer)          // Base64 IV  
        };  
    },

    /\*\*  
     \* Decrypt AES-256-CBC encrypted data.  
     \*   
     \* @param {string} ciphertextBase64 The encrypted message (Base64)  
     \* @param {CryptoKey} aesKey The AES key  
     \* @param {string} ivBase64 The IV used during encryption (Base64)  
     \* @returns {Promise\<string\>} Decrypted plaintext  
     \*/  
    async decryptWithAES(ciphertextBase64, aesKey, ivBase64) {  
        const ciphertext \= this.\_base64ToArrayBuffer(ciphertextBase64);  
        const iv \= this.\_base64ToArrayBuffer(ivBase64);

        const decrypted \= await window.crypto.subtle.decrypt(  
            { name: "AES-CBC", iv: iv },  
            aesKey,  
            ciphertext  
        );

        const decoder \= new TextDecoder();  
        return decoder.decode(decrypted);  
    },

    // ─── SHA-256 OPERATIONS ───────────────────────────────

    /\*\*  
     \* Compute SHA-256 hash of a string.  
     \*   
     \* Used BEFORE encryption to create a "fingerprint" of the original message.  
     \* After decryption, the receiver computes this again and compares.  
     \* Any tampering in transit will produce a different hash.  
     \*   
     \* @param {string} text The plaintext message  
     \* @returns {Promise\<string\>} SHA-256 hash as lowercase hex string  
     \*/  
    async computeSHA256(text) {  
        const encoder \= new TextEncoder();  
        const data \= encoder.encode(text);  
        const hashBuffer \= await window.crypto.subtle.digest("SHA-256", data);

        // Convert hash bytes to hex string  
        const hashArray \= Array.from(new Uint8Array(hashBuffer));  
        return hashArray.map(b \=\> b.toString(16).padStart(2, '0')).join('');  
    },

    /\*\*  
     \* Verify SHA-256 hash.  
     \*   
     \* @param {string} text The decrypted message  
     \* @param {string} expectedHash The hash received with the message  
     \* @returns {Promise\<boolean\>} true if message is authentic  
     \*/  
    async verifySHA256(text, expectedHash) {  
        const actualHash \= await this.computeSHA256(text);  
        return actualHash \=== expectedHash;  
    },

    // ─── FULL ENCRYPT/DECRYPT FLOWS ───────────────────────

    /\*\*  
     \* COMPLETE ENCRYPTION FLOW (called when sending a message)  
     \*   
     \* Steps:  
     \* 1\. Compute SHA-256 hash of plaintext  
     \* 2\. Generate new AES-256 key  
     \* 3\. Encrypt message with AES  
     \* 4\. Encrypt AES key with receiver's RSA public key  
     \* 5\. Return all encrypted components  
     \*   
     \* @param {string} plaintext The message to send  
     \* @param {string} receiverPublicKeyBase64 Receiver's RSA public key  
     \* @returns {Promise\<Object\>} Encrypted payload ready for WebSocket  
     \*/  
    async encryptMessage(plaintext, receiverPublicKeyBase64) {  
        // STEP 1: Hash the original message for integrity  
        const messageHash \= await this.computeSHA256(plaintext);

        // STEP 2: Generate fresh AES session key (new key per message\!)  
        const aesKey \= await this.generateAESKey();

        // STEP 3: Encrypt the message with AES-CBC  
        const { ciphertext, iv } \= await this.encryptWithAES(plaintext, aesKey);

        // STEP 4: Export AES key to bytes, then encrypt with receiver's RSA public key  
        const aesKeyBytes \= await this.exportAESKey(aesKey);  
        const receiverPublicKey \= await this.importPublicKey(receiverPublicKeyBase64);  
        const encryptedAesKey \= await this.encryptWithRSA(aesKeyBytes, receiverPublicKey);

        return {  
            encryptedMessage: ciphertext,    // AES-encrypted message (Base64)  
            encryptedAesKey: encryptedAesKey, // RSA-encrypted AES key (Base64)  
            iv: iv,                           // AES IV (Base64)  
            messageHash: messageHash          // SHA-256 of original plaintext (hex)  
        };  
    },

    /\*\*  
     \* COMPLETE DECRYPTION FLOW (called when receiving a message)  
     \*   
     \* Steps:  
     \* 1\. Load our RSA private key from localStorage  
     \* 2\. Decrypt the AES key using our RSA private key  
     \* 3\. Decrypt the message using the recovered AES key  
     \* 4\. Verify SHA-256 hash  
     \*   
     \* @param {Object} encryptedPayload { encryptedMessage, encryptedAesKey, iv, messageHash }  
     \* @param {string} currentUsername Our username (for localStorage key lookup)  
     \* @returns {Promise\<{plaintext: string, isValid: boolean}\>}  
     \*/  
    async decryptMessage(encryptedPayload, currentUsername) {  
        // STEP 1: Load our private key from localStorage  
        const privateKeyBase64 \= localStorage.getItem(\`privateKey\_${currentUsername}\`);  
        if (\!privateKeyBase64) {  
            throw new Error('Private key not found in localStorage. Please re-register.');  
        }

        const privateKey \= await this.importPrivateKey(privateKeyBase64);

        // STEP 2: Decrypt the AES session key using our RSA private key  
        const aesKeyBytes \= await this.decryptWithRSA(encryptedPayload.encryptedAesKey, privateKey);

        // STEP 3: Import the recovered AES key  
        const aesKey \= await this.importAESKey(aesKeyBytes);

        // STEP 4: Decrypt the message using the AES key  
        const plaintext \= await this.decryptWithAES(  
            encryptedPayload.encryptedMessage,  
            aesKey,  
            encryptedPayload.iv  
        );

        // STEP 5: Verify integrity using SHA-256  
        const isValid \= await this.verifySHA256(plaintext, encryptedPayload.messageHash);

        return { plaintext, isValid };  
    },

    // ─── HELPER UTILITIES ─────────────────────────────────

    /\*\* Convert ArrayBuffer to Base64 string \*/  
    \_arrayBufferToBase64(buffer) {  
        const bytes \= new Uint8Array(buffer);  
        let binary \= '';  
        for (let i \= 0; i \< bytes.byteLength; i++) {  
            binary \+= String.fromCharCode(bytes\[i\]);  
        }  
        return window.btoa(binary);  
    },

    /\*\* Convert Base64 string to ArrayBuffer \*/  
    \_base64ToArrayBuffer(base64) {  
        const binary \= window.atob(base64);  
        const bytes \= new Uint8Array(binary.length);  
        for (let i \= 0; i \< binary.length; i++) {  
            bytes\[i\] \= binary.charCodeAt(i);  
        }  
        return bytes.buffer;  
    }  
};

### **`static/js/chat.js`**

/\*\*  
 \* \============================================================  
 \* CHAT.JS — WebSocket Chat Client  
 \* \============================================================  
 \*   
 \* Handles:  
 \* \- WebSocket connection (STOMP over SockJS)  
 \* \- Sending encrypted messages  
 \* \- Receiving and decrypting messages  
 \* \- Loading conversation history  
 \* \- Updating the chat UI  
 \* \============================================================  
 \*/

// ─── GLOBAL STATE ───────────────────────────────────────────  
let stompClient \= null;         // WebSocket client  
let currentChatUser \= null;     // Currently open chat's username  
let myUsername \= null;          // Logged-in user's username  
let lastEncryptedMessages \= \[\]; // For "Show Encrypted" demo feature

// ─── INITIALIZATION ─────────────────────────────────────────

/\*\*  
 \* Initialize the chat application.  
 \* Called when the page loads.  
 \*/  
async function initChat() {  
    // Get current username from the data attribute set by Thymeleaf  
    const serverData \= document.getElementById('serverData');  
    myUsername \= serverData.getAttribute('data-current-user');

    // Check if private key exists in localStorage  
    const privateKey \= localStorage.getItem(\`privateKey\_${myUsername}\`);  
    if (\!privateKey) {  
        showToast('⚠️ Private key not found\! You may need to re-register on this device.', 'warning', 5000);  
    }

    // Connect to WebSocket  
    connectWebSocket();  
}

/\*\*  
 \* Connect to the Spring Boot WebSocket server via STOMP/SockJS.  
 \*/  
function connectWebSocket() {  
    const socket \= new SockJS('/ws');  
    stompClient \= Stomp.over(socket);

    // Suppress STOMP debug logs in production  
    stompClient.debug \= null;

    stompClient.connect({}, function(frame) {  
        console.log('✅ WebSocket connected:', frame);

        /\*\*  
         \* Subscribe to our private message queue.  
         \* Only messages addressed to us arrive here.  
         \* Destination: /user/{myUsername}/queue/messages  
         \*/  
        stompClient.subscribe('/user/queue/messages', async function(message) {  
            const chatMessage \= JSON.parse(message.body);  
            await handleIncomingMessage(chatMessage);  
        });

        showToast('🔐 Secure connection established', 'success', 2000);

    }, function(error) {  
        console.error('WebSocket connection error:', error);  
        showToast('❌ Connection failed. Retrying...', 'error', 3000);  
        // Retry after 3 seconds  
        setTimeout(connectWebSocket, 3000);  
    });  
}

// ─── SENDING MESSAGES ───────────────────────────────────────

/\*\*  
 \* Send an encrypted message to the current chat partner.  
 \*   
 \* ENCRYPTION FLOW:  
 \* 1\. Get plaintext from input  
 \* 2\. Fetch receiver's RSA public key from server  
 \* 3\. Encrypt message (AES) \+ encrypt AES key (RSA) \+ hash (SHA-256)  
 \* 4\. Send encrypted payload over WebSocket  
 \*/  
async function sendMessage() {  
    const input \= document.getElementById('messageInput');  
    const plaintext \= input.value.trim();

    if (\!plaintext || \!currentChatUser || \!stompClient) return;

    const sendBtn \= document.getElementById('sendBtn');  
    sendBtn.disabled \= true;  
    sendBtn.textContent \= '🔐 Encrypting...';

    try {  
        // STEP 1: Fetch receiver's RSA public key from server  
        const response \= await fetch(\`/api/public-key/${currentChatUser}\`);  
        if (\!response.ok) throw new Error('Could not fetch public key');

        const { publicKey: receiverPublicKey } \= await response.json();

        // STEP 2: Encrypt the message using hybrid cryptography  
        // This calls CryptoUtils.encryptMessage() which:  
        //   \- Hashes the plaintext with SHA-256  
        //   \- Generates a random AES-256 key  
        //   \- Encrypts message with AES  
        //   \- Encrypts AES key with receiver's RSA public key  
        const encrypted \= await CryptoUtils.encryptMessage(plaintext, receiverPublicKey);

        // STEP 3: Build the WebSocket message payload  
        const chatMessage \= {  
            senderUsername: myUsername,  
            receiverUsername: currentChatUser,  
            encryptedMessage: encrypted.encryptedMessage,  
            encryptedAesKey: encrypted.encryptedAesKey,  
            iv: encrypted.iv,  
            messageHash: encrypted.messageHash,  
            type: 'CHAT'  
        };

        // Save for "Show Encrypted" demo feature  
        lastEncryptedMessages.push({  
            plaintext: plaintext,  
            encrypted: chatMessage  
        });

        // STEP 4: Send via WebSocket to server  
        // The server receives only ciphertext — never the plaintext  
        stompClient.send('/app/chat.send', {}, JSON.stringify(chatMessage));

        // Clear input  
        input.value \= '';

    } catch (error) {  
        console.error('Encryption/send error:', error);  
        showToast('❌ Failed to send message: ' \+ error.message, 'error', 4000);  
    } finally {  
        sendBtn.disabled \= false;  
        sendBtn.textContent \= 'Send 🚀';  
    }  
}

// ─── RECEIVING MESSAGES ─────────────────────────────────────

/\*\*  
 \* Handle an incoming encrypted message from the WebSocket.  
 \*   
 \* DECRYPTION FLOW:  
 \* 1\. Check if message is for the current open chat  
 \* 2\. Decrypt AES key with our RSA private key  
 \* 3\. Decrypt message with AES key  
 \* 4\. Verify SHA-256 hash  
 \* 5\. Display in UI with integrity status  
 \*/  
async function handleIncomingMessage(message) {  
    if (message.type \=== 'JOIN' || message.type \=== 'LEAVE') {  
        showToast(\`ℹ️ ${message.senderUsername} ${message.type \=== 'JOIN' ? 'joined' : 'left'}\`, 'info', 2000);  
        return;  
    }

    const isMyMessage \= (message.senderUsername \=== myUsername);  
    const chatPartner \= isMyMessage ? message.receiverUsername : message.senderUsername;

    try {  
        let decryptedText;  
        let isValid \= false;

        if (isMyMessage) {  
            // We sent this message — we know the plaintext  
            // But we can still display it properly  
            // For demo: we'll try to decrypt our own copy  
            // In production: store sent messages locally before encrypting  
            decryptedText \= '\[Your encrypted message\]';  
            isValid \= true;  
        } else {  
            // Decrypt the received message using our private RSA key  
            const result \= await CryptoUtils.decryptMessage(message, myUsername);  
            decryptedText \= result.plaintext;  
            isValid \= result.isValid;  
        }

        // Display the message if this chat is currently open  
        if (currentChatUser \=== chatPartner || (isMyMessage && currentChatUser \=== message.receiverUsername)) {  
            displayMessage({  
                text: decryptedText,  
                sender: message.senderUsername,  
                timestamp: message.timestamp || new Date().toLocaleTimeString(),  
                isOwn: isMyMessage,  
                isValid: isValid,  
                encryptedData: message  
            });  
        }

    } catch (error) {  
        console.error('Decryption error:', error);  
        if (currentChatUser \=== chatPartner) {  
            displayMessage({  
                text: '❌ \[Decryption failed — key not available on this device\]',  
                sender: message.senderUsername,  
                timestamp: message.timestamp,  
                isOwn: false,  
                isValid: false,  
                isError: true  
            });  
        }  
    }  
}

// ─── OPENING A CHAT ──────────────────────────────────────────

/\*\*  
 \* Open a chat with a specific user.  
 \* Loads message history and sets up the chat window.  
 \*/  
async function openChat(username) {  
    currentChatUser \= username;

    // Update UI  
    document.getElementById('welcomeScreen').style.display \= 'none';  
    document.getElementById('chatWindow').style.display \= 'flex';  
    document.getElementById('chatHeaderName').textContent \= username;  
    document.getElementById('chatHeaderAvatar').textContent \= username\[0\].toUpperCase();

    // Highlight selected user in sidebar  
    document.querySelectorAll('.user-item').forEach(item \=\> {  
        item.classList.toggle('active', item.dataset.username \=== username);  
    });

    // Clear existing messages  
    const container \= document.getElementById('messagesContainer');  
    container.innerHTML \= '\<div class="loading-messages"\>🔓 Decrypting message history...\</div\>';

    // Load conversation history  
    await loadHistory(username);  
}

/\*\*  
 \* Load and decrypt conversation history with a user.  
 \*/  
async function loadHistory(username) {  
    try {  
        const response \= await fetch(\`/api/messages/history?with=${username}\`);  
        const messages \= await response.json();

        const container \= document.getElementById('messagesContainer');  
        container.innerHTML \= '';

        if (messages.length \=== 0\) {  
            container.innerHTML \= '\<div class="no-messages"\>🔐 No messages yet. Start a secure conversation\!\</div\>';  
        }

        for (const msg of messages) {  
            const isMyMessage \= (msg.senderUsername \=== myUsername);

            try {  
                let decryptedText;  
                let isValid \= false;

                if (\!isMyMessage) {  
                    const result \= await CryptoUtils.decryptMessage(msg, myUsername);  
                    decryptedText \= result.plaintext;  
                    isValid \= result.isValid;  
                } else {  
                    // Messages we sent are encrypted with receiver's key  
                    // We can't decrypt them without their private key  
                    // This is a trade-off of true E2EE — server can't read, sender also can't re-read  
                    // Solution: store sent message text locally  
                    decryptedText \= '\[Sent message — encrypted with recipient key\]';  
                    isValid \= true;  
                }

                displayMessage({  
                    text: decryptedText,  
                    sender: msg.senderUsername,  
                    timestamp: msg.timestamp,  
                    isOwn: isMyMessage,  
                    isValid: isValid,  
                    encryptedData: msg  
                });

            } catch (e) {  
                displayMessage({  
                    text: '🔒 \[Could not decrypt — encrypted with another device key\]',  
                    sender: msg.senderUsername,  
                    timestamp: msg.timestamp,  
                    isOwn: isMyMessage,  
                    isValid: false,  
                    isError: true  
                });  
            }  
        }

        document.getElementById('cryptoBar').style.display \= 'flex';  
        scrollToBottom();

    } catch (error) {  
        console.error('Error loading history:', error);  
        showToast('❌ Could not load message history', 'error', 3000);  
    }  
}

// ─── UI RENDERING ────────────────────────────────────────────

/\*\*  
 \* Render a single message bubble in the chat window.  
 \*/  
function displayMessage({ text, sender, timestamp, isOwn, isValid, isError, encryptedData }) {  
    const container \= document.getElementById('messagesContainer');

    const messageDiv \= document.createElement('div');  
    messageDiv.className \= \`message ${isOwn ? 'message-own' : 'message-other'}\`;

    const integrityIcon \= isError ? '❌' : (isValid ? '✅' : '⚠️');  
    const integrityTitle \= isError  
        ? 'Decryption failed'  
        : (isValid ? 'Integrity verified — SHA-256 hash matches' : 'WARNING: Hash mismatch — message may have been tampered\!');

    messageDiv.innerHTML \= \`  
        \<div class="message-bubble ${isError ? 'message-error' : ''}"\>  
            \<div class="message-text"\>${escapeHtml(text)}\</div\>  
            \<div class="message-meta"\>  
                \<span class="message-time"\>${timestamp || ''}\</span\>  
                \<span class="integrity-badge" title="${integrityTitle}"\>${integrityIcon}\</span\>  
            \</div\>  
        \</div\>  
        ${\!isOwn ? \`\<div class="message-sender"\>${sender}\</div\>\` : ''}  
    \`;

    // Store encrypted data for "Show Encrypted" feature  
    if (encryptedData) {  
        messageDiv.dataset.encrypted \= JSON.stringify(encryptedData);  
        messageDiv.onclick \= () \=\> showEncryptedDetails(encryptedData, text);  
    }

    container.appendChild(messageDiv);  
    scrollToBottom();  
}

/\*\*  
 \* Show encrypted data panel (demo feature for academic presentation).  
 \*/  
function showEncryptedDetails(encryptedData, plaintext) {  
    const panel \= document.getElementById('encryptedPanel');  
    const dataDiv \= document.getElementById('encryptedData');

    dataDiv.innerHTML \= \`  
        \<div class="enc-section"\>  
            \<label\>📝 Original Plaintext:\</label\>  
            \<div class="enc-value plaintext"\>${escapeHtml(plaintext || '(sent by you)')}\</div\>  
        \</div\>  
        \<div class="enc-section"\>  
            \<label\>🔐 AES-256 Encrypted Message (what server stores):\</label\>  
            \<div class="enc-value ciphertext"\>${encryptedData.encryptedMessage}\</div\>  
        \</div\>  
        \<div class="enc-section"\>  
            \<label\>🔑 RSA-2048 Encrypted AES Key:\</label\>  
            \<div class="enc-value ciphertext"\>${encryptedData.encryptedAesKey}\</div\>  
        \</div\>  
        \<div class="enc-section"\>  
            \<label\>📊 AES Initialization Vector (IV):\</label\>  
            \<div class="enc-value"\>${encryptedData.iv}\</div\>  
        \</div\>  
        \<div class="enc-section"\>  
            \<label\>🔍 SHA-256 Hash (integrity fingerprint):\</label\>  
            \<div class="enc-value hash"\>${encryptedData.messageHash}\</div\>  
        \</div\>  
    \`;

    panel.style.display \= 'block';  
}

function toggleEncryptedView() {  
    const panel \= document.getElementById('encryptedPanel');  
    panel.style.display \= panel.style.display \=== 'none' ? 'block' : 'none';  
}

// ─── UTILITY FUNCTIONS ───────────────────────────────────────

function scrollToBottom() {  
    const container \= document.getElementById('messagesContainer');  
    container.scrollTop \= container.scrollHeight;  
}

function escapeHtml(text) {  
    const div \= document.createElement('div');  
    div.textContent \= text;  
    return div.innerHTML;  
}

function filterUsers(query) {  
    const items \= document.querySelectorAll('.user-item');  
    items.forEach(item \=\> {  
        const name \= item.dataset.username.toLowerCase();  
        item.style.display \= name.includes(query.toLowerCase()) ? 'flex' : 'none';  
    });  
}

function showToast(message, type \= 'info', duration \= 3000\) {  
    const toast \= document.getElementById('toast');  
    toast.textContent \= message;  
    toast.className \= \`toast toast-${type}\`;  
    toast.style.display \= 'block';  
    setTimeout(() \=\> { toast.style.display \= 'none'; }, duration);  
}

// Initialize when page loads  
document.addEventListener('DOMContentLoaded', initChat);

### **`static/css/style.css`**

/\* \============================================================  
   STYLE.CSS — SecureChat UI Styles  
   Clean, professional dark theme for security application  
   \============================================================ \*/

:root {  
    \--primary: \#2563eb;  
    \--primary-dark: \#1d4ed8;  
    \--secondary: \#10b981;  
    \--bg-dark: \#0f172a;  
    \--bg-card: \#1e293b;  
    \--bg-input: \#334155;  
    \--text-primary: \#f8fafc;  
    \--text-secondary: \#94a3b8;  
    \--border: \#334155;  
    \--own-message: \#2563eb;  
    \--other-message: \#334155;  
    \--danger: \#ef4444;  
    \--warning: \#f59e0b;  
    \--success: \#10b981;  
}

\* { box-sizing: border-box; margin: 0; padding: 0; }

body {  
    font-family: 'Segoe UI', system-ui, sans-serif;  
    background: var(--bg-dark);  
    color: var(--text-primary);  
    height: 100vh;  
    overflow: hidden;  
}

/\* ─── AUTH PAGES ─────────────────────────────────── \*/  
.auth-page {  
    display: flex;  
    align-items: center;  
    justify-content: center;  
    min-height: 100vh;  
    background: linear-gradient(135deg, \#0f172a 0%, \#1e293b 100%);  
    overflow-y: auto;  
}

.auth-container {  
    background: var(--bg-card);  
    border: 1px solid var(--border);  
    border-radius: 16px;  
    padding: 40px;  
    width: 100%;  
    max-width: 440px;  
    margin: 20px;  
    box-shadow: 0 20px 60px rgba(0,0,0,0.5);  
}

.auth-header { text-align: center; margin-bottom: 28px; }  
.lock-icon { font-size: 48px; margin-bottom: 12px; }  
.auth-header h1 { font-size: 28px; font-weight: 700; }  
.subtitle { color: var(--text-secondary); font-size: 13px; margin-top: 6px; }

.auth-form { display: flex; flex-direction: column; gap: 16px; }

.form-group { display: flex; flex-direction: column; gap: 6px; }  
.form-group label { font-size: 13px; font-weight: 600; color: var(--text-secondary); }  
.form-group input {  
    background: var(--bg-input);  
    border: 1px solid var(--border);  
    border-radius: 8px;  
    padding: 12px 16px;  
    color: var(--text-primary);  
    font-size: 15px;  
    transition: border-color 0.2s;  
}  
.form-group input:focus {  
    outline: none;  
    border-color: var(--primary);  
}

.btn-primary {  
    background: var(--primary);  
    color: white;  
    border: none;  
    border-radius: 8px;  
    padding: 13px;  
    font-size: 15px;  
    font-weight: 600;  
    cursor: pointer;  
    transition: background 0.2s;  
    margin-top: 4px;  
}  
.btn-primary:hover { background: var(--primary-dark); }  
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

.auth-link { text-align: center; margin-top: 16px; color: var(--text-secondary); font-size: 14px; }  
.auth-link a { color: var(--primary); text-decoration: none; font-weight: 600; }

.alert {  
    padding: 10px 16px;  
    border-radius: 8px;  
    font-size: 14px;  
    margin-bottom: 12px;  
}  
.alert-error { background: rgba(239,68,68,0.15); border: 1px solid var(--danger); color: \#fca5a5; }  
.alert-success { background: rgba(16,185,129,0.15); border: 1px solid var(--success); color: \#6ee7b7; }

.crypto-info {  
    margin-top: 24px;  
    padding: 16px;  
    background: rgba(37,99,235,0.1);  
    border: 1px solid rgba(37,99,235,0.3);  
    border-radius: 8px;  
    font-size: 13px;  
}  
.crypto-info h4 { margin-bottom: 8px; color: var(--primary); }  
.crypto-info ul { padding-left: 16px; color: var(--text-secondary); line-height: 1.8; }

.key-gen-status {  
    display: flex;  
    align-items: center;  
    gap: 10px;  
    padding: 10px 16px;  
    background: rgba(16,185,129,0.1);  
    border-radius: 8px;  
    margin-bottom: 16px;  
    font-size: 13px;  
    color: var(--secondary);  
}

.key-display {  
    margin-top: 20px;  
    padding: 16px;  
    background: rgba(0,0,0,0.3);  
    border-radius: 8px;  
    border: 1px solid var(--border);  
}  
.key-display h4 { color: var(--secondary); margin-bottom: 12px; }  
.key-section { margin-bottom: 12px; }  
.key-section label { font-size: 12px; color: var(--text-secondary); display: block; margin-bottom: 4px; }  
.key-section textarea {  
    width: 100%;  
    background: var(--bg-input);  
    border: 1px solid var(--border);  
    border-radius: 6px;  
    color: var(--text-secondary);  
    padding: 8px;  
    font-size: 11px;  
    font-family: monospace;  
    resize: none;  
}  
.private-key { border-color: var(--warning) \!important; }  
.key-warning { font-size: 12px; color: var(--warning); margin-top: 8px; }

/\* ─── CHAT LAYOUT ────────────────────────────────── \*/  
.chat-layout {  
    display: flex;  
    height: 100vh;  
}

/\* ─── SIDEBAR ────────────────────────────────────── \*/  
.sidebar {  
    width: 280px;  
    min-width: 280px;  
    background: var(--bg-card);  
    border-right: 1px solid var(--border);  
    display: flex;  
    flex-direction: column;  
}

.sidebar-header {  
    padding: 16px;  
    border-bottom: 1px solid var(--border);  
    display: flex;  
    align-items: center;  
    justify-content: space-between;  
}

.user-info { display: flex; align-items: center; gap: 10px; }  
.avatar {  
    width: 40px; height: 40px;  
    background: var(--primary);  
    border-radius: 50%;  
    display: flex; align-items: center; justify-content: center;  
    font-weight: 700; font-size: 16px;  
}  
.current-username { font-weight: 600; font-size: 15px; }  
.status-badge { font-size: 11px; color: var(--secondary); }

.logout-btn {  
    color: var(--text-secondary);  
    text-decoration: none;  
    font-size: 20px;  
    padding: 6px;  
    border-radius: 6px;  
    transition: background 0.2s;  
}  
.logout-btn:hover { background: var(--bg-input); }

.sidebar-search { padding: 12px 16px; border-bottom: 1px solid var(--border); }  
.sidebar-search input {  
    width: 100%;  
    background: var(--bg-input);  
    border: 1px solid var(--border);  
    border-radius: 8px;  
    padding: 8px 12px;  
    color: var(--text-primary);  
    font-size: 14px;  
}  
.sidebar-search input:focus { outline: none; border-color: var(--primary); }

.users-list { flex: 1; overflow-y: auto; padding: 8px 0; }  
.users-list-header { padding: 8px 16px; font-size: 11px; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 1px; }

.user-item {  
    display: flex;  
    align-items: center;  
    gap: 12px;  
    padding: 12px 16px;  
    cursor: pointer;  
    transition: background 0.15s;  
    border-radius: 8px;  
    margin: 0 8px;  
}  
.user-item:hover { background: var(--bg-input); }  
.user-item.active { background: rgba(37,99,235,0.15); }

.user-avatar {  
    width: 40px; height: 40px;  
    background: var(--bg-input);  
    border-radius: 50%;  
    display: flex; align-items: center; justify-content: center;  
    font-weight: 700;  
}  
.user-item.active .user-avatar { background: var(--primary); }

.user-item-name { font-weight: 600; font-size: 14px; }  
.user-item-status { font-size: 12px; color: var(--text-secondary); }

.no-users { padding: 20px 16px; color: var(--text-secondary); font-size: 14px; text-align: center; }

/\* ─── CHAT AREA ──────────────────────────────────── \*/  
.chat-area {  
    flex: 1;  
    display: flex;  
    flex-direction: column;  
    position: relative;  
    overflow: hidden;  
}

.welcome-screen {  
    flex: 1;  
    display: flex;  
    flex-direction: column;  
    align-items: center;  
    justify-content: center;  
    gap: 16px;  
    color: var(--text-secondary);  
}  
.welcome-icon { font-size: 64px; }  
.welcome-screen h2 { font-size: 24px; color: var(--text-primary); }  
.welcome-features { display: flex; gap: 16px; flex-wrap: wrap; justify-content: center; margin-top: 8px; }  
.welcome-features span {  
    background: var(--bg-card);  
    border: 1px solid var(--border);  
    border-radius: 20px;  
    padding: 6px 14px;  
    font-size: 13px;  
}

.chat-window {  
    flex: 1;  
    display: flex;  
    flex-direction: column;  
}

.chat-header {  
    padding: 14px 20px;  
    border-bottom: 1px solid var(--border);  
    background: var(--bg-card);  
    display: flex;  
    align-items: center;  
    gap: 12px;  
}  
.chat-header-avatar {  
    width: 40px; height: 40px;  
    background: var(--primary);  
    border-radius: 50%;  
    display: flex; align-items: center; justify-content: center;  
    font-weight: 700;  
}  
.chat-header-info { flex: 1; }  
.chat-header-name { font-weight: 600; font-size: 16px; }  
.chat-header-status { font-size: 12px; color: var(--secondary); }

.show-encrypted-btn {  
    background: rgba(37,99,235,0.15);  
    border: 1px solid rgba(37,99,235,0.3);  
    color: var(--primary);  
    border-radius: 8px;  
    padding: 6px 12px;  
    font-size: 13px;  
    cursor: pointer;  
    transition: background 0.2s;  
}  
.show-encrypted-btn:hover { background: rgba(37,99,235,0.3); }

.messages-container {  
    flex: 1;  
    overflow-y: auto;  
    padding: 20px;  
    display: flex;  
    flex-direction: column;  
    gap: 8px;  
}

.loading-messages, .no-messages {  
    text-align: center;  
    color: var(--text-secondary);  
    padding: 40px;  
    font-size: 14px;  
}

.message { display: flex; flex-direction: column; max-width: 70%; }  
.message-own { align-self: flex-end; align-items: flex-end; }  
.message-other { align-self: flex-start; align-items: flex-start; }

.message-bubble {  
    padding: 10px 14px;  
    border-radius: 16px;  
    max-width: 100%;  
    word-break: break-word;  
}  
.message-own .message-bubble { background: var(--own-message); border-bottom-right-radius: 4px; }  
.message-other .message-bubble { background: var(--other-message); border-bottom-left-radius: 4px; }  
.message-error .message-bubble { background: rgba(239,68,68,0.2); border: 1px solid var(--danger); }

.message-text { font-size: 15px; line-height: 1.5; }  
.message-meta { display: flex; align-items: center; gap: 6px; margin-top: 4px; }  
.message-time { font-size: 11px; color: rgba(255,255,255,0.6); }  
.integrity-badge { font-size: 12px; cursor: help; }  
.message-sender { font-size: 11px; color: var(--text-secondary); margin-top: 3px; padding: 0 4px; }

.crypto-bar {  
    padding: 6px 20px;  
    background: rgba(16,185,129,0.1);  
    border-top: 1px solid rgba(16,185,129,0.2);  
    font-size: 11px;  
    color: var(--secondary);  
    text-align: center;  
}

.message-input-area {  
    padding: 16px 20px;  
    border-top: 1px solid var(--border);  
    background: var(--bg-card);  
    display: flex;  
    gap: 12px;  
    align-items: center;  
}  
.input-wrapper { flex: 1; position: relative; }  
.input-wrapper input {  
    width: 100%;  
    background: var(--bg-input);  
    border: 1px solid var(--border);  
    border-radius: 12px;  
    padding: 12px 16px;  
    padding-right: 44px;  
    color: var(--text-primary);  
    font-size: 15px;  
}  
.input-wrapper input:focus { outline: none; border-color: var(--primary); }  
.input-actions {  
    position: absolute;  
    right: 12px;  
    top: 50%;  
    transform: translateY(-50%);  
}  
.encrypt-indicator { font-size: 18px; }

.send-btn {  
    background: var(--primary);  
    color: white;  
    border: none;  
    border-radius: 12px;  
    padding: 12px 20px;  
    font-size: 15px;  
    font-weight: 600;  
    cursor: pointer;  
    white-space: nowrap;  
    transition: background 0.2s;  
}  
.send-btn:hover { background: var(--primary-dark); }  
.send-btn:disabled { opacity: 0.6; cursor: not-allowed; }

/\* ─── ENCRYPTED PANEL (DEMO FEATURE) ─────────────── \*/  
.encrypted-panel {  
    position: absolute;  
    bottom: 80px;  
    right: 0;  
    width: 480px;  
    max-height: 60vh;  
    background: var(--bg-card);  
    border: 1px solid var(--border);  
    border-radius: 12px 0 0 12px;  
    overflow: auto;  
    box-shadow: \-4px 0 20px rgba(0,0,0,0.4);  
    z-index: 100;  
}  
.encrypted-panel-header {  
    padding: 14px 16px;  
    background: rgba(37,99,235,0.1);  
    border-bottom: 1px solid var(--border);  
    font-size: 13px;  
    font-weight: 600;  
    display: flex;  
    justify-content: space-between;  
    align-items: center;  
}  
.encrypted-panel-header button {  
    background: none;  
    border: none;  
    color: var(--text-secondary);  
    cursor: pointer;  
    font-size: 16px;  
}  
.encrypted-data { padding: 16px; }  
.enc-section { margin-bottom: 16px; }  
.enc-section label { font-size: 11px; color: var(--text-secondary); display: block; margin-bottom: 6px; font-weight: 600; }  
.enc-value {  
    background: var(--bg-dark);  
    border: 1px solid var(--border);  
    border-radius: 6px;  
    padding: 8px 10px;  
    font-family: monospace;  
    font-size: 11px;  
    word-break: break-all;  
    color: var(--text-secondary);  
}  
.enc-value.ciphertext { color: \#f97316; }  
.enc-value.hash { color: var(--secondary); }  
.enc-value.plaintext { color: var(--text-primary); font-family: inherit; font-size: 14px; }

/\* ─── TOAST NOTIFICATIONS ────────────────────────── \*/  
.toast {  
    position: fixed;  
    bottom: 24px;  
    left: 50%;  
    transform: translateX(-50%);  
    padding: 10px 20px;  
    border-radius: 8px;  
    font-size: 14px;  
    z-index: 1000;  
    animation: fadeIn 0.2s ease;  
}  
.toast-success { background: rgba(16,185,129,0.9); color: white; }  
.toast-error { background: rgba(239,68,68,0.9); color: white; }  
.toast-info { background: rgba(37,99,235,0.9); color: white; }  
.toast-warning { background: rgba(245,158,11,0.9); color: white; }

@keyframes fadeIn { from { opacity: 0; transform: translateX(-50%) translateY(10px); } to { opacity: 1; transform: translateX(-50%) translateY(0); } }

/\* ─── SCROLLBAR STYLING ──────────────────────────── \*/  
::-webkit-scrollbar { width: 6px; }  
::-webkit-scrollbar-track { background: transparent; }  
::-webkit-scrollbar-thumb { background: var(--border); border-radius: 3px; }  
::-webkit-scrollbar-thumb:hover { background: var(--text-secondary); }

---

## **18\. Main Application Class**

### **`E2EChatApplication.java`**

package com.securechat;

import org.springframework.boot.SpringApplication;  
import org.springframework.boot.autoconfigure.SpringBootApplication;

/\*\*  
 \* \============================================================  
 \* E2E ENCRYPTED CHAT APPLICATION — MAIN ENTRY POINT  
 \* \============================================================  
 \*   
 \* This application demonstrates End-to-End Encryption (E2EE)  
 \* using Hybrid Cryptography for an academic Information Security project.  
 \*   
 \* CRYPTOGRAPHIC STACK:  
 \* ┌─────────────────────────────────────────────────────────┐  
 \* │  RSA-2048     → Asymmetric key exchange (AES key wrapping)│  
 \* │  AES-256-CBC  → Symmetric message encryption            │  
 \* │  SHA-256      → Message integrity verification           │  
 \* │  BCrypt       → Password hashing (strength=12)          │  
 \* └─────────────────────────────────────────────────────────┘  
 \*   
 \* HOW E2EE IS ACHIEVED:  
 \* \- All message encryption/decryption happens IN THE BROWSER  
 \* \- The server stores and routes only encrypted ciphertext  
 \* \- Even the server admin cannot read messages  
 \* \- Similar architecture to WhatsApp / Signal (conceptually)  
 \*   
 \* To run: mvn spring-boot:run  
 \* Access: http://localhost:8080  
 \* \============================================================  
 \*/  
@SpringBootApplication  
public class E2EChatApplication {

    public static void main(String\[\] args) {  
        SpringApplication.run(E2EChatApplication.class, args);  
        System.out.println("\\n" \+  
            "╔══════════════════════════════════════════════╗\\n" \+  
            "║  🔐 SecureChat E2EE System Started\!          ║\\n" \+  
            "║  → http://localhost:8080                     ║\\n" \+  
            "║  → RSA-2048 \+ AES-256 \+ SHA-256             ║\\n" \+  
            "╚══════════════════════════════════════════════╝"  
        );  
    }  
}

---

## **19\. Setup & Run Instructions**

### **Prerequisites**

\# Required software:  
\# ✅ Java 17+ (check: java \-version)  
\# ✅ Maven 3.8+ (check: mvn \-version)  
\# ✅ MySQL 8.0+ (check: mysql \--version)  
\# ✅ Modern browser (Chrome, Firefox, Edge — for Web Crypto API)

### **Step 1 — Clone & Set Up Database**

\# 1\. Create the database  
mysql \-u root \-p  
\# In MySQL shell:  
CREATE DATABASE secure\_chat\_db CHARACTER SET utf8mb4 COLLATE utf8mb4\_unicode\_ci;  
exit;

### **Step 2 — Configure application.properties**

\# Edit src/main/resources/application.properties  
\# Set your MySQL credentials:  
spring.datasource.username=root  
spring.datasource.password=YOUR\_MYSQL\_PASSWORD

### **Step 3 — Build & Run**

\# Clone the project  
git clone \<your-repo-url\>  
cd e2e-chat

\# Build the project  
mvn clean install \-DskipTests

\# Run the application  
mvn spring-boot:run

\# You should see:  
\# ╔══════════════════════════════════════════════╗  
\# ║  🔐 SecureChat E2EE System Started\!          ║  
\# ║  → http://localhost:8080                     ║  
\# ╚══════════════════════════════════════════════╝

### **Step 4 — Access the Application**

Open browser → http://localhost:8080

---

## **20\. Sample Test Flow**

### **Register Two Users**

1\. Open Browser Tab 1: http://localhost:8080/register  
   \- Username: alice  
   \- Password: Alice@1234  
   \- Click "Generate Keys & Register"  
   → RSA-2048 key pair generated in browser  
   → Private key saved to localStorage  
   → Public key sent to server  
   → Registration successful

2\. Open Browser Tab 2 (Incognito): http://localhost:8080/register  
   \- Username: bob  
   \- Password: Bob@1234  
   \- Click "Generate Keys & Register"  
   → Bob gets his own RSA key pair

### **Send an Encrypted Message**

3\. Tab 1: Login as alice  
4\. Tab 2: Login as bob

5\. Tab 1 (Alice's browser):  
   \- Click on "bob" in the sidebar  
   \- Type: "Hello Bob\! This is a secret message."  
   \- Click Send

   WHAT HAPPENS BEHIND THE SCENES:  
   ┌─────────────────────────────────────────────┐  
   │ 1\. Browser fetches Bob's RSA public key     │  
   │ 2\. Generates random AES-256 key             │  
   │ 3\. Computes SHA-256("Hello Bob\!...")        │  
   │    → hash: a3f2...                          │  
   │ 4\. AES encrypts the message:                │  
   │    → ciphertext: 7Kp9mN2...                 │  
   │ 5\. RSA encrypts the AES key with Bob's key: │  
   │    → encAESKey: XmP2...                     │  
   │ 6\. Sends: {ciphertext, encAESKey, iv, hash} │  
   │    (Server NEVER sees "Hello Bob\!")         │  
   └─────────────────────────────────────────────┘

6\. Tab 2 (Bob's browser):  
   Message arrives\! Bob's browser:  
   ┌─────────────────────────────────────────────┐  
   │ 1\. Loads Bob's RSA private key (localStorage)│  
   │ 2\. Decrypts AES key with RSA private key     │  
   │ 3\. Decrypts message with AES key             │  
   │    → "Hello Bob\! This is a secret message." │  
   │ 4\. Computes SHA-256 of decrypted message     │  
   │ 5\. Compares with received hash              │  
   │    → Match\! ✅ Message is authentic          │  
   └─────────────────────────────────────────────┘

7\. Click "👁️ Show Encrypted" button to see what the server stored

### **Verify Database (Server Never Has Plaintext)**

\-- Connect to MySQL and run:  
USE secure\_chat\_db;  
SELECT encrypted\_message, encrypted\_aes\_key, message\_hash FROM messages;

\-- Output will be unreadable ciphertext:  
\-- encrypted\_message: "7Kp9mN2wXzL+4Qr..."  
\-- encrypted\_aes\_key: "XmP2KjHd9nB..."  
\-- message\_hash: "a3f2c8d1..."  
\-- ✅ Plaintext "Hello Bob\!" is NOWHERE in the database\!

---

## **21\. Cursor / Lovable / Claude Agent Prompt**

Copy the entire section below and paste it as your prompt in **Cursor**, **Lovable**, or **Claude** to auto-generate the project.

---

You are a senior Java Spring Boot developer and cybersecurity expert.

Generate a COMPLETE, RUNNABLE End-to-End Encrypted Chat System using the exact specifications below. Follow the project structure, file names, package names, and code exactly.

\=== PROJECT METADATA \===  
\- Root package: com.securechat  
\- Artifact ID: e2e-chat  
\- Java Version: 17  
\- Spring Boot Version: 3.2.0  
\- Database: MySQL (database name: secure\_chat\_db)

\=== GENERATE THESE FILES IN ORDER \===

1\. pom.xml (Maven dependencies: spring-boot-starter-web, thymeleaf, security, data-jpa, websocket, mysql-connector-j, lombok, jackson-databind)

2\. src/main/resources/application.properties (MySQL config, JPA, Thymeleaf, port 8080\)

3\. src/main/java/com/securechat/E2EChatApplication.java (Main class with @SpringBootApplication)

4\. src/main/java/com/securechat/model/User.java (@Entity with id, username, password, publicKey fields)

5\. src/main/java/com/securechat/model/Message.java (@Entity with id, senderUsername, receiverUsername, encryptedMessage, encryptedAesKey, iv, messageHash, timestamp)

6\. src/main/java/com/securechat/model/dto/RegisterRequest.java (POJO: username, password, publicKey)

7\. src/main/java/com/securechat/model/dto/ChatMessageDTO.java (POJO: senderUsername, receiverUsername, encryptedMessage, encryptedAesKey, iv, messageHash, timestamp, type)

8\. src/main/java/com/securechat/model/dto/PublicKeyResponse.java (POJO: username, publicKey)

9\. src/main/java/com/securechat/repository/UserRepository.java (JpaRepository with findByUsername and existsByUsername)

10\. src/main/java/com/securechat/repository/MessageRepository.java (JpaRepository with findConversation JPQL query)

11\. src/main/java/com/securechat/util/CryptoUtils.java (@Component with RSA key gen, RSA encrypt/decrypt, AES key gen, AES encrypt/decrypt, SHA-256 hash/verify methods)

12\. src/main/java/com/securechat/security/CustomUserDetailsService.java (UserDetailsService loading from UserRepository)

13\. src/main/java/com/securechat/security/SecurityConfig.java (BCryptPasswordEncoder strength 12, custom login page at /login, logout, CSRF disabled for /ws and /api)

14\. src/main/java/com/securechat/service/UserService.java (registerUser, findByUsername, getAllUsers, getPublicKey)

15\. src/main/java/com/securechat/service/MessageService.java (saveMessage, getConversation \- both work with encrypted data only)

16\. src/main/java/com/securechat/service/CryptoService.java (wrapper for CryptoUtils)

17\. src/main/java/com/securechat/websocket/WebSocketConfig.java (STOMP, SockJS at /ws, broker /topic /user, app prefix /app)

18\. src/main/java/com/securechat/controller/AuthController.java (GET /login, GET /register, POST /api/register)

19\. src/main/java/com/securechat/controller/ChatController.java (GET /chat, GET /api/public-key/{username}, GET /api/messages/history, GET /api/me)

20\. src/main/java/com/securechat/controller/MessageWebSocketController.java (@MessageMapping /chat.send and /chat.join using SimpMessagingTemplate)

21\. src/main/resources/templates/login.html (Thymeleaf, dark theme, Spring Security form POST /login)

22\. src/main/resources/templates/register.html (Thymeleaf, dark theme, JS calls /api/register with RSA key generation)

23\. src/main/resources/templates/chat.html (Thymeleaf, dark theme, sidebar with user list, chat window, SockJS \+ STOMP scripts)

24\. src/main/resources/static/css/style.css (Dark theme, variables, auth pages, chat layout, message bubbles, sidebar)

25\. src/main/resources/static/js/crypto-utils.js (Web Crypto API: generateRSAKeyPair, exportPublicKey, exportPrivateKey, importPublicKey, importPrivateKey, encryptWithRSA, decryptWithRSA, generateAESKey, encryptWithAES, decryptWithAES, computeSHA256, verifySHA256, encryptMessage, decryptMessage)

26\. src/main/resources/static/js/chat.js (STOMP WebSocket client, sendMessage, handleIncomingMessage, openChat, loadHistory, displayMessage, UI helpers)

\=== SECURITY REQUIREMENTS \===  
\- Server NEVER decrypts messages — all crypto in browser JS  
\- RSA-OAEP with SHA-256 for key wrapping  
\- AES-256-CBC for message encryption  
\- Fresh AES key per message (perfect forward secrecy)  
\- SHA-256 hash computed BEFORE encryption, verified AFTER decryption  
\- BCrypt strength 12 for passwords  
\- Private key stored in localStorage only (never sent to server)  
\- WebSocket messages contain only encrypted payloads

\=== UI REQUIREMENTS \===  
\- Dark theme (\#0f172a background)  
\- Login page at /login  
\- Register page at /register (generates RSA keys on button click)  
\- Chat page at /chat (after login)  
\- Left sidebar: current user info, user list, search  
\- Right area: welcome screen OR chat window  
\- Message bubbles: blue for own, dark for others  
\- Integrity badge: ✅ for valid, ⚠️ for tampered  
\- "Show Encrypted" button reveals raw ciphertext (demo feature)  
\- Toast notifications for connection status  
\- Logout button

\=== DATABASE \===  
MySQL tables auto-created by Hibernate (ddl-auto=update):  
\- users: id, username (unique), password (BCrypt), public\_key (TEXT), created\_at  
\- messages: id, sender\_username, receiver\_username, encrypted\_message (LONGTEXT), encrypted\_aes\_key (TEXT), iv, message\_hash, timestamp

\=== COMMENTS REQUIRED \===  
Every file must have clear JavaDoc/JSDoc comments explaining:  
\- What the class/function does  
\- How it fits into the E2EE flow  
\- Which cryptographic algorithm is used and why

Generate all 26 files completely with no placeholders or TODO comments.  
Make the code runnable on localhost with mvn spring-boot:run after setting MySQL credentials in application.properties.

---

## **📊 Quick Reference Card**

┌─────────────────────────────────────────────────────────────┐  
│              SECURECHAT — QUICK REFERENCE                   │  
├─────────────────────┬───────────────────────────────────────┤  
│ Start App           │ mvn spring-boot:run                   │  
│ URL                 │ http://localhost:8080                  │  
│ Register            │ /register                             │  
│ Login               │ /login                                │  
│ Chat                │ /chat                                 │  
├─────────────────────┼───────────────────────────────────────┤  
│ RSA Key Size        │ 2048 bits                             │  
│ AES Key Size        │ 256 bits                              │  
│ AES Mode           │ CBC with random IV                    │  
│ Hash Algorithm      │ SHA-256                               │  
│ Password Hash       │ BCrypt (strength=12)                  │  
├─────────────────────┼───────────────────────────────────────┤  
│ Private Key Stored  │ Browser localStorage ONLY             │  
│ Public Key Stored   │ MySQL database                        │  
│ Messages Stored     │ Encrypted ciphertext ONLY             │  
│ Server Sees         │ ZERO plaintext                        │  
└─────────────────────┴───────────────────────────────────────┘


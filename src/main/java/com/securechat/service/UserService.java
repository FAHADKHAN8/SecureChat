package com.securechat.service;

import com.securechat.model.User;
import com.securechat.model.dto.RegisterRequest;
import com.securechat.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user registration and lookup so authentication and recipient public-key discovery can
 * support the browser-managed end-to-end encryption flow.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user by hashing the password with BCrypt and storing the browser-generated
     * RSA public key that other users need for AES key wrapping.
     */
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPublicKey(request.getPublicKey());
        return userRepository.save(user);
    }

    /**
     * Finds a user by username so controllers and security components can resolve recipients.
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Returns all registered users to populate the chat sidebar with possible recipients.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Returns a user's RSA public key, which senders use with RSA-OAEP to encrypt per-message
     * AES session keys before transmitting ciphertext.
     */
    public String getPublicKey(String username) {
        return userRepository.findByUsername(username)
                .map(User::getPublicKey)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

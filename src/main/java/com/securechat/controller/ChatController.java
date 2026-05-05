package com.securechat.controller;

import com.securechat.model.dto.ChatMessageDTO;
import com.securechat.model.dto.PublicKeyResponse;
import com.securechat.service.MessageService;
import com.securechat.service.UserService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Serves the authenticated chat UI and exposes lookup endpoints that let browsers fetch public
 * keys and encrypted history needed for the end-to-end encryption workflow.
 */
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final UserService userService;
    private final MessageService messageService;

    @GetMapping({"/", "/chat"})
    public String chatPage(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        List<String> userList = userService.getAllUsers().stream()
                .map(com.securechat.model.User::getUsername)
                .filter(username -> !username.equals(currentUser.getUsername()))
                .toList();

        model.addAttribute("currentUser", currentUser.getUsername());
        model.addAttribute("userList", userList);
        return "chat";
    }

    @GetMapping("/api/public-key/{username}")
    @ResponseBody
    public ResponseEntity<PublicKeyResponse> getPublicKey(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(null);
        }

        return userService.findByUsername(username)
                .map(user -> ResponseEntity.ok(new PublicKeyResponse(user.getUsername(), user.getPublicKey())))
                .orElseGet(() -> ResponseEntity.status(404).body(null));
    }

    @GetMapping("/api/messages/history")
    @ResponseBody
    public ResponseEntity<List<ChatMessageDTO>> getHistory(
            @RequestParam("with") String otherUsername,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.ok(messageService.getConversation(currentUser.getUsername(), otherUsername));
    }

    @GetMapping("/api/me")
    @ResponseBody
    public ResponseEntity<Map<String, String>> me(@AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(Map.of("username", currentUser.getUsername()));
    }
}

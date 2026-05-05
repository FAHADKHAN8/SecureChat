package com.securechat.repository;

import com.securechat.model.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Loads encrypted message history without ever exposing plaintext because the database stores
 * only ciphertext-related fields produced by the browser crypto layer.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE "
            + "(m.senderUsername = :user1 AND m.receiverUsername = :user2) OR "
            + "(m.senderUsername = :user2 AND m.receiverUsername = :user1) "
            + "ORDER BY m.timestamp ASC")
    List<Message> findConversation(@Param("user1") String user1, @Param("user2") String user2);

    List<Message> findByReceiverUsernameOrderByTimestampDesc(String receiverUsername);
}

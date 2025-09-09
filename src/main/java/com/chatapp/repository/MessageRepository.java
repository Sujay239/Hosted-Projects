package com.chatapp.repository;

import com.chatapp.model.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface MessageRepository extends JpaRepository<MessageEntity,Long>
{
    @Query("SELECT m FROM MessageEntity m " +
            "WHERE (m.sender = :userA AND m.receiver = :userB) " +
            "   OR (m.sender = :userB AND m.receiver = :userA) " +
            "AND (m.type = 'PRIVATE')"+
            "ORDER BY m.timestamp ASC")
    List<MessageEntity> getConversation(@Param("userA") String userA,
                                        @Param("userB") String userB);
}

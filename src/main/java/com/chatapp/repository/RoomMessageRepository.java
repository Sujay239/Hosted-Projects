package com.chatapp.repository;

import com.chatapp.model.RoomMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomMessageRepository extends JpaRepository<RoomMessageEntity, Long> {
    List<RoomMessageEntity> findByRoomId(String roomId);
}

package com.chatapp.repository;

import com.chatapp.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface RoomRepository extends JpaRepository<Room, String> {
    List<Room> findByCreatedBy(String createdBy);
    @Query(value = "SELECT * FROM rooms r WHERE JSON_CONTAINS(r.participants, CONCAT('\"', :user, '\"'))", nativeQuery = true)
    Set<Room> findByParticipant(@Param("user") String user);
}

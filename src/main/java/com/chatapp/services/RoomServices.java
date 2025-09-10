package com.chatapp.services;

import com.chatapp.model.Room;
import com.chatapp.repository.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.*;

@Slf4j
@Service
public class RoomServices {

    @Autowired
    private RoomRepository roomRepository;


    public Room createRoom(String name, String roomName, String roomDescription) {
        Room room = new Room();
        room.setCreatedBy(name);
        room.setName(roomName);
        room.setDescription(roomDescription);
        room.getParticipants().add(name);
        room.setId(UUID.randomUUID().toString());
        return roomRepository.save(room);
    }

    public Room getRoomById(String roomId) {
        return roomRepository.findById(roomId).orElse(null);
    }


    public ResponseEntity<?> joinRoom(String roomId, String username) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            log.info("Room found: {}", roomOpt.get().getName());
            Room room = roomOpt.get();
            if (!room.getParticipants().contains(username)) {
                room.getParticipants().add(username);
                Room save = roomRepository.save(room);
                return ResponseEntity.ok(save);
            } else {
                return ResponseEntity.badRequest().body("User already in the room.");
            }
        } else {
            log.info("Room not found with ID: {}", roomId);
            return ResponseEntity.status(404).body("Room not found.");

        }
    }

public Set<Room> findRoomsByParticipant(String user) {
    return roomRepository.findByParticipant(user);
}

}

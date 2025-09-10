package com.chatapp.controller;

import com.chatapp.model.Room;
import com.chatapp.repository.RoomRepository;
import com.chatapp.services.RoomServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "https://sujay-secure-chatapp.netlify.app", allowedHeaders = "*", allowCredentials = "true")
public class RoomController {

@Autowired
    private RoomRepository roomRepository;
    @Autowired
    private RoomServices roomServices;


    @PostMapping("/create")
    public ResponseEntity<Room> createRoom(@RequestBody Map<String,String> room, Principal principal) {
        String name = principal.getName();
        String roomName = room.get("name");
        String roomDescription = room.get("description");
        Room room1 =  roomServices.createRoom(name, roomName, roomDescription);
        return ResponseEntity.ok(room1);
    }

    @GetMapping("/my")
    public ResponseEntity<Set<Room>> getMyRooms(@RequestParam("user") String name) {
        return ResponseEntity.ok(roomServices.findRoomsByParticipant(name));
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId,
                                      @RequestParam String username) {
       return roomServices.joinRoom(roomId, username);
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<?> leaveRoom(@PathVariable String roomId,
                                       @RequestBody Map<String, String> payload) {
        String username = payload.get("username");

        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Room currentRoom = roomOpt.get();
        Set<String> users = currentRoom.getParticipants();

        if (!users.contains(username)) {
            return ResponseEntity.badRequest().body("User not in this room");
        }

        // Remove the user
        users.remove(username);

        if (users.isEmpty()) {
            // Delete room if no participants left
            roomRepository.deleteById(roomId);
            return ResponseEntity.ok("Room deleted as no participants remain.");
        } else {
            // Save updated room
            currentRoom.setParticipants(users);
            roomRepository.save(currentRoom);
            return ResponseEntity.ok("You successfully left the room.");
        }
    }


    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoomById(@PathVariable String roomId) {
        return roomRepository.findById(roomId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



//    /api/rooms/${roomId}/join?username=${username}


//    @GetMapping
//    public List<Room> getAllRooms() {
//        return roomRepository.findAll();
//    }
}

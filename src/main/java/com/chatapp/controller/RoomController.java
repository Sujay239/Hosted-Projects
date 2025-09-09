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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
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
    public ResponseEntity<List<Room>> getMyRooms(@RequestParam("user") String name) {
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

        return roomRepository.findById(roomId)
                .map(room -> {
                    if (room.getParticipants().contains(username)) {
                        room.getParticipants().remove(username);
                        roomRepository.save(room);
                        return ResponseEntity.ok("User removed from room");
                    } else {
                        return ResponseEntity.badRequest().body("User not in this room");
                    }
                })
                .orElse(ResponseEntity.notFound().build());
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

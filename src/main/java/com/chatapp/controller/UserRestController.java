package com.chatapp.controller;

import com.chatapp.model.Users;
import com.chatapp.security.JwtProvider;
import com.chatapp.services.OnlineUserService;
import com.chatapp.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "https://sujay-secure-chatapp.netlify.app", allowedHeaders = "*", allowCredentials = "true")
public class UserRestController {

    private final UserService userService;
    @Autowired
    private  JwtProvider jwtProvider;
    @Autowired
    private OnlineUserService onlineUserService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/online")
    public List<String> getOnlineUsers() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getOnlineUsers()
                .stream()
                .map(Users::getUsername)          // only send username
                .filter(username -> !username.equals(currentUser)) // exclude self
                .collect(Collectors.toList());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            String username = jwtProvider.extractUsername(jwt); // use your JwtUtil

            // mark user offline in your tracking system
            onlineUserService.removeUser(username);
        }
        return ResponseEntity.ok("Logged out successfully");
    }


}

package com.chatapp.controller;

import com.chatapp.dto.AuthResponse;
import com.chatapp.dto.LoginRequests;
import com.chatapp.dto.RegisterRequest;
import com.chatapp.model.Users;
import com.chatapp.repository.UserRepository;
import com.chatapp.services.AuthService;
import com.chatapp.services.OnlineUserService;
import com.chatapp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "https://sujay-secure-chatapp.netlify.app", allowedHeaders = "*", allowCredentials = "true") // allow frontend calls
public class AuthController {
    private final AuthService authService;
    @Autowired
    private  UserRepository userRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private OnlineUserService onlineUserService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        String token = authService.register(req);
        return ResponseEntity.ok(new AuthResponse(token, req.getUsername(),req.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequests req) {
        try {
            String token = authService.login(req);
            Users user = userRepo.findUsersByEmail(req.getUsername()).get();
            String name = user.getUsername();
            userService.setUserOnline(name);
            onlineUserService.addUser(name);
            return ResponseEntity.ok(new AuthResponse(token, name,user.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(e.getMessage(),null,null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String username) {
        userService.setUserOffline(username);
        onlineUserService.removeUser(username);
        return ResponseEntity.ok("User logged out successfully");
    }


}

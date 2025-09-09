package com.chatapp.services;

import com.chatapp.dto.LoginRequests;
import com.chatapp.dto.RegisterRequest;
import com.chatapp.model.Users;
import com.chatapp.repository.UserRepository;
import com.chatapp.security.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtUtil;

    // Use constructor injection for all dependencies
    public AuthService(UserRepository userRepo,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String register(RegisterRequest req) {
        // Check if username already exists
        if (userRepo.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        // Check if email already exists
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already taken");
        }

        // Create and save user
        Users user = new Users();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        Users savedUser = userRepo.save(user);
        return jwtUtil.generateToken(savedUser.getUsername());
    }

    public String login(LoginRequests req) {
        Users user = userRepo.findUsersByEmail(req.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return jwtUtil.generateToken(user.getUsername());
    }
}
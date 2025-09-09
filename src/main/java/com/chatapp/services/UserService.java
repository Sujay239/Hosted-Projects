package com.chatapp.services;

import com.chatapp.model.Users;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void setUserOnline(String username) {
        Users user = userRepository.findUsersByUsername(username).get();
        user.setOnline(true);
        userRepository.save(user);
    }

    public void setUserOffline(String username) {
        Users user = userRepository.findUsersByUsername(username).get();
        user.setOnline(false);
        userRepository.save(user);
    }

    public List<Users> getOnlineUsers() {
        return userRepository.findByOnlineTrue();
    }
}

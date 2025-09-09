package com.chatapp.repository;

import com.chatapp.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface UserRepository extends JpaRepository<Users,Long> {
    Optional<Users> findUsersByUsername(String name);
    Optional<Users> findUsersByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<Users> findByOnlineTrue();
}

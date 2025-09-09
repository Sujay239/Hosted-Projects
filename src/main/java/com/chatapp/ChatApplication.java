package com.chatapp;

import com.chatapp.security.JwtProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatApplication {

	public static void main(String[] args) {
        String secret = "G7miUqFu3Ec8inDLEtzBPJZqeWNVaMtt/biIApSOU8Y=";
        long expiration = 3600000; // 1 hour

        JwtProvider jwtProvider = new JwtProvider(secret, expiration);
        String token = jwtProvider.generateToken("Nishika Adak");
        System.out.println("Generated JWT: " + token);

        // Test validation
        System.out.println("Valid? " + jwtProvider.validateToken(token));
        System.out.println("Username: " + jwtProvider.extractUsername(token));
		SpringApplication.run(ChatApplication.class, args);
	}

}

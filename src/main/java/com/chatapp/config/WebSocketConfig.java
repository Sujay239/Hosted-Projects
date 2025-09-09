package com.chatapp.config;
//
//import com.chatapp.security.JwtProvider;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.web.socket.config.annotation.*;
//import org.springframework.messaging.simp.config.ChannelRegistration;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//
//import java.security.Principal;
//
//@Configuration
//@EnableWebSocketMessageBroker
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//
//    private final JwtProvider jwtProvider;
//
//    public WebSocketConfig(JwtProvider jwtProvider) {
//        this.jwtProvider = jwtProvider;
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/ws")
//                .setAllowedOriginPatterns("*")
//                .withSockJS();
//    }
//
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        registry.enableSimpleBroker("/topic", "/queue");
//        registry.setApplicationDestinationPrefixes("/app");
//        registry.setUserDestinationPrefix("/user");
//    }
//
//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(new ChannelInterceptor() {
//            @Override
//            public Message<?> preSend(Message<?> message, MessageChannel channel) {
//                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//
//                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//                    String authHeader = accessor.getFirstNativeHeader("Authorization");
//                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                        String token = authHeader.substring(7);
//                        String username = jwtProvider.extractUsername(token);
//                        if (username != null && jwtProvider.validateToken(token)) {
//                            accessor.setUser(new Principal() {
//                                @Override
//                                public String getName() {
//                                    return username;
//                                }
//                            });
//                        }
//                    }
//                }
//                return message;
//            }
//        });
//    }
//}


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("https://sujay-secure-chatapp.netlify.app")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue", "/user");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}


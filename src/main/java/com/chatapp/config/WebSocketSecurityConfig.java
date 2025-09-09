//package com.chatapp.config;
//
//import com.chatapp.security.JwtProvider;
//import com.chatapp.services.CustomUserDetailsService;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
//import org.springframework.messaging.simp.config.ChannelRegistration;
//import org.springframework.messaging.support.ChannelInterceptor;
//
//@Configuration
//public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {
//
//    private final JwtProvider jwtProvider;
//    private final CustomUserDetailsService userDetailsService;
//
//    public WebSocketSecurityConfig(JwtProvider jwtProvider, CustomUserDetailsService userDetailsService) {
//        this.jwtProvider = jwtProvider;
//        this.userDetailsService = userDetailsService;
//    }
//
//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(new ChannelInterceptor() {
//            @Override
//            public org.springframework.messaging.Message<?> preSend(org.springframework.messaging.Message<?> message, org.springframework.messaging.MessageChannel channel) {
//
//                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//
//                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//                    String authHeader = accessor.getFirstNativeHeader("Authorization");
//                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                        String token = authHeader.substring(7);
//
//                        if (jwtProvider.validateToken(token)) {
//                            String username = jwtProvider.extractUsername(token);
//                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//
//                            UsernamePasswordAuthenticationToken auth =
//                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//                            accessor.setUser(auth);
//                            SecurityContextHolder.getContext().setAuthentication(auth);
//                        }
//                    }
//                }
//                return message;
//            }
//        });
//    }
//}

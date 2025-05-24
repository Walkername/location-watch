package ru.locationwatch.backend.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import ru.locationwatch.backend.services.TokenService;

/**
 * Component to check stomp headers on JWT Token
 * Endpoint "/ws" is permitted, so to check if websocket connection from authenticated user (ADMIN)
 */

@Component
public class JWTChannelInterceptor implements ChannelInterceptor {

    private final TokenService tokenService;

    @Autowired
    public JWTChannelInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Extract token from headers
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    tokenService.validateAccessToken(token);
                } catch (JWTVerificationException e) {
                    throw new AuthenticationCredentialsNotFoundException("Invalid JWT token");
                }
            } else {
                throw new AuthenticationCredentialsNotFoundException("Token missing");
            }
        }
        return message;
    }

}

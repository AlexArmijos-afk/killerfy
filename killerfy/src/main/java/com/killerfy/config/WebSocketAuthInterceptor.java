package com.killerfy.config;

import com.killerfy.security.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    public WebSocketAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    // Usar los métodos exactos de tu JwtUtil
                    if (jwtUtil.esValido(token)) {
                        String email = jwtUtil.extraerEmail(token);
                        String rol   = jwtUtil.extraerRol(token);

                        // Guardar en sesión para poder identificar el dispositivo en SessionDisconnectEvent
                        Map<String, Object> attrs = accessor.getSessionAttributes();
                        if (attrs != null) {
                            attrs.put("email", email);
                            try {
                                attrs.put("dispositivo", jwtUtil.extraerTipoDispositivo(token).name());
                            } catch (Exception ignored) {}
                        }

                        UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                            );
                        accessor.setUser(auth);
                    }
                } catch (Exception ignored) {}
            }
        }
        return message;
    }
}
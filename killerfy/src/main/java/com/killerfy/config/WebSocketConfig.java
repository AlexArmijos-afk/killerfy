package com.killerfy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	private final WebSocketAuthInterceptor authInterceptor;
	
	public WebSocketConfig(WebSocketAuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
	    registry.addEndpoint("/ws")
	            .setAllowedOriginPatterns("*");
	}

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // El cliente envía mensajes a /app/...
        registry.setApplicationDestinationPrefixes("/app");
        // El broker retransmite a /topic/... y /user/.../queue/...
        registry.enableSimpleBroker("/topic", "/user");
        // Prefijo para mensajes dirigidos a un usuario concreto
        registry.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void configureClientInboundChannel(
            org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(authInterceptor); // ← JWT en WS
    }
}
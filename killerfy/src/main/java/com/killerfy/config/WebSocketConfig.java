package com.killerfy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
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
        // Heartbeats 10s/10s: el servidor anuncia a los clientes que enviará y
        // espera pings cada 10 s. Sin esto, las conexiones quedaban idle y los
        // proxies/firewalls/Capacitor en background cerraban el TCP en silencio,
        // perdiéndose los eventos publicados al topic durante el corte.
        registry.enableSimpleBroker("/topic", "/user")
                .setHeartbeatValue(new long[] { 10000, 10000 })
                .setTaskScheduler(heartbeatScheduler());
        // Prefijo para mensajes dirigidos a un usuario concreto
        registry.setUserDestinationPrefix("/user");
    }

    @Bean
    public TaskScheduler heartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    // El límite por defecto de Spring STOMP es 64 KB. Con colas de cientos de
    // canciones los eventos CAMBIAR_CANCION / SIGUIENTE excedían ese límite y
    // el servidor cerraba la conexión con código 1009 (Message Too Big),
    // haciendo que ningún evento llegara a los demás dispositivos.
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration
            .setMessageSizeLimit(1024 * 1024)      // 1 MB por mensaje entrante
            .setSendBufferSizeLimit(2 * 1024 * 1024) // 2 MB buffer de salida
            .setSendTimeLimit(20_000);               // 20 s timeout de envío
    }

    @Override
    public void configureClientInboundChannel(
            org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(authInterceptor); // ← JWT en WS
    }
}
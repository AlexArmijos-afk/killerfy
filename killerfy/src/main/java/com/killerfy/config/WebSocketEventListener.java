package com.killerfy.config;

import com.killerfy.dto.ReproductorEvent;
import com.killerfy.model.Dispositivo.TipoDispositivo;
import com.killerfy.model.SesionDispositivo;
import com.killerfy.service.SesionDispositivoService;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Map;

@Component
public class WebSocketEventListener {

    private final SesionDispositivoService sesionService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(SesionDispositivoService sesionService,
                                   SimpMessagingTemplate messagingTemplate) {
        this.sesionService = sesionService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> attrs = sha.getSessionAttributes();
        if (attrs == null) return;

        String email = (String) attrs.get("email");
        String dispositivo = (String) attrs.get("dispositivo");
        if (email == null || dispositivo == null) return;

        try {
            List<SesionDispositivo> sesiones = sesionService.obtenerSesionesPorEmail(email);
            boolean estabaReproduciendo = sesiones.stream()
                .anyMatch(s -> s.getDispositivo().getTipo().name().equals(dispositivo)
                            && Boolean.TRUE.equals(s.getReproduciendo()));

            if (estabaReproduciendo) {
                // Limpiar estado de reproducción en DB
                sesionService.actualizarReproduciendo(email, List.of());

                // dispositivosActivos vacío indica a los demás dispositivos que deben
                // limpiar el selector "Reproduciendo en" (distinción con PAUSE normal)
                ReproductorEvent pause = new ReproductorEvent(ReproductorEvent.Tipo.PAUSE, email);
                pause.setDispositivo(dispositivo);
                pause.setDispositivosActivos(List.of());
                messagingTemplate.convertAndSend("/topic/reproductor/" + email, pause);
            }

            // Marcar el dispositivo como desconectado
            sesionService.cerrarSesion(email, TipoDispositivo.valueOf(dispositivo));
        } catch (Exception ignored) {}
    }
}

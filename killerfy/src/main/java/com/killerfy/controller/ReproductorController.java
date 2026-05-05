package com.killerfy.controller;

import com.killerfy.dto.ReproductorEvent;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ReproductorController {

    private final SimpMessagingTemplate messagingTemplate;

    public ReproductorController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Recibe eventos de un dispositivo del usuario
     * y los retransmite a TODOS los dispositivos del mismo usuario.
     * El cliente envía a: /app/reproductor
     * El broker retransmite a: /topic/reproductor/{email}
     */
    @MessageMapping("/reproductor")
    public void procesarEvento(@Payload ReproductorEvent evento, Principal principal) {
        // Asociar el email del usuario autenticado al evento
        String email = principal.getName();
        evento.setUsuarioEmail(email);

        // Broadcast a todos los dispositivos del usuario
        messagingTemplate.convertAndSend(
            "/topic/reproductor/" + email,
            evento
        );
    }

    /**
     * Envía un evento solo al dispositivo destino (para TRANSFERIR).
     * El cliente envía a: /app/reproductor/transferir
     */
    @MessageMapping("/reproductor/transferir")
    public void transferirReproduccion(@Payload ReproductorEvent evento, Principal principal) {
        String email = principal.getName();
        evento.setUsuarioEmail(email);

        // Enviar a todos los dispositivos para que el destino se active
        // y los demás se desactiven
        messagingTemplate.convertAndSend(
            "/topic/reproductor/" + email,
            evento
        );
    }
}
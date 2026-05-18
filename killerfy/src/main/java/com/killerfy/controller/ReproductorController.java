package com.killerfy.controller;

import com.killerfy.dto.ReproductorEvent;
import com.killerfy.model.Dispositivo;
import com.killerfy.service.SesionDispositivoService;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ReproductorController {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, ReproductorEvent> estadoActual = new ConcurrentHashMap<>();
    private final SesionDispositivoService sesionDispositivoService;

    public ReproductorController(SimpMessagingTemplate messagingTemplate,
            SesionDispositivoService sesionDispositivoService) {
this.messagingTemplate = messagingTemplate;
this.sesionDispositivoService = sesionDispositivoService;
}

    @MessageMapping("/reproductor")
    public void procesarEvento(@Payload ReproductorEvent evento, Principal principal) {
        if (principal == null) {
            System.out.println("[WS] ERROR: principal es null, token no autenticado");
            return;
        }

        String email = principal.getName();
        System.out.println("[WS] Evento recibido de: " + email + " tipo: " + evento.getTipo());
        evento.setUsuarioEmail(email);

        // Guardar estado para sincronización al entrar
        actualizarEstado(email, evento);

        messagingTemplate.convertAndSend("/topic/reproductor/" + email, evento);
    }

    @MessageMapping("/reproductor/transferir")
    public void transferirReproduccion(@Payload ReproductorEvent evento, Principal principal) {
        String email = principal.getName();
        evento.setUsuarioEmail(email);

        // Actualizar en BD qué dispositivos deben sonar
        if (evento.getDispositivosActivos() != null && !evento.getDispositivosActivos().isEmpty()) {
            sesionDispositivoService.actualizarDispositivosSonando(
                email, evento.getDispositivosActivos()
            );
        }

        // Retransmitir a todos los dispositivos del usuario
        messagingTemplate.convertAndSend("/topic/reproductor/" + email, evento);
    }

    @GetMapping("/api/reproductor/estado")
    @ResponseBody
    public ResponseEntity<ReproductorEvent> getEstado(Principal principal) {
        if (principal == null) return ResponseEntity.noContent().build();
        ReproductorEvent estado = estadoActual.get(principal.getName());
        if (estado == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(estado);
    }

    private void actualizarEstado(String email, ReproductorEvent evento) {
        switch (evento.getTipo()) {
            case CAMBIAR_CANCION:
                // Canción nueva: guarda todo el evento
                estadoActual.put(email, evento);
                break;
            case SEEK:
                // Solo actualiza el progreso si ya hay un estado
                estadoActual.computeIfPresent(email, (k, est) -> {
                    est.setProgreso(evento.getProgreso());
                    return est;
                });
                break;
            case PLAY:
            case PAUSE:
                // Solo actualiza el tipo (reproduciendo / pausado)
                estadoActual.computeIfPresent(email, (k, est) -> {
                    est.setTipo(evento.getTipo());
                    return est;
                });
                break;
            default:
                break;
        }
    }
}
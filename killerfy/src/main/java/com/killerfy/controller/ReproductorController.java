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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
        if (principal == null) return;
        String email = principal.getName();
        evento.setUsuarioEmail(email);
        actualizarEstado(email, evento);

        if (evento.getTipo() == ReproductorEvent.Tipo.CAMBIAR_CANCION) {
            // ✅ Respetar quién debe sonar según el frontend, NO sobreescribir con el emisor
            List<String> dispositivosQueDebenSonar = (evento.getDispositivosActivos() != null
                    && !evento.getDispositivosActivos().isEmpty())
                ? evento.getDispositivosActivos()
                : (evento.getDispositivo() != null ? List.of(evento.getDispositivo()) : List.of());

            if (!dispositivosQueDebenSonar.isEmpty()) {
                sesionDispositivoService.actualizarReproduciendo(email, dispositivosQueDebenSonar);
            }
            // NO sobreescribir evento.dispositivosActivos — ya viene correcto del frontend
        }

        // TRANSFERIR: ya estaba bien
        if (evento.getTipo() == ReproductorEvent.Tipo.TRANSFERIR
                && evento.getDispositivosActivos() != null
                && !evento.getDispositivosActivos().isEmpty()) {
            sesionDispositivoService.actualizarReproduciendo(
                email, evento.getDispositivosActivos());
        }

        messagingTemplate.convertAndSend("/topic/reproductor/" + email, evento);
    }

    @MessageMapping("/reproductor/transferir")
    public void transferirReproduccion(@Payload ReproductorEvent evento, Principal principal) {
        String email = principal.getName();
        evento.setUsuarioEmail(email);

        if (evento.getDispositivosActivos() != null 
            && !evento.getDispositivosActivos().isEmpty()) {
            // ✅ Este método debe existir y escribir en columna 'reproduciendo'
            sesionDispositivoService.actualizarReproduciendo(
                email, evento.getDispositivosActivos()
            );
        }

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
                // Canción nueva: guarda todo el evento (incluye cola)
                estadoActual.put(email, evento);
                break;
            case SIGUIENTE:
            case ANTERIOR:
                // Actualizar cancionId, cola y progreso para que un dispositivo
                // que conecte después del SIGUIENTE/ANTERIOR reciba el estado correcto.
                estadoActual.computeIfPresent(email, (k, est) -> {
                    if (evento.getCancionId() != null) {
                        est.setCancionId(evento.getCancionId());
                    }
                    if (evento.getCola() != null) {
                        est.setCola(evento.getCola());
                    }
                    est.setProgreso(0.0);
                    est.setTipo(ReproductorEvent.Tipo.CAMBIAR_CANCION);
                    return est;
                });
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
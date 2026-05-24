package com.killerfy.controller;

import com.killerfy.dto.LoginRequest;
import com.killerfy.dto.RegistroRequest;
import com.killerfy.dto.ReproductorEvent;
import com.killerfy.model.Dispositivo.TipoDispositivo;
import com.killerfy.model.SesionDispositivo;
import com.killerfy.model.Usuario;
import com.killerfy.security.JwtUtil;
import com.killerfy.service.SesionDispositivoService;
import com.killerfy.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final SesionDispositivoService sesionDispositivoService;
    private final SimpMessagingTemplate messagingTemplate; // ← añadido al final

    // ✅ Todo por constructor, sin @Autowired en campo
    public AuthController(UsuarioService usuarioService,
                          JwtUtil jwtUtil,
                          SesionDispositivoService sesionDispositivoService,
                          SimpMessagingTemplate messagingTemplate) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
        this.sesionDispositivoService = sesionDispositivoService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(request.getEmail());
        if (usuarioOpt.isEmpty())
            return ResponseEntity.status(401).body(Map.of("error", "Email o contraseña incorrectos"));
        Usuario usuario = usuarioOpt.get();
        if (!usuarioService.verificarPassword(request.getPassword(), usuario.getPassword()))
            return ResponseEntity.status(401).body(Map.of("error", "Email o contraseña incorrectos"));

        List<String> roles = usuario.getRoles().stream()
                .map(r -> r.getNombreRol().name())
                .collect(Collectors.toList());
        TipoDispositivo tipoDispositivo = request.getTipoDispositivo();
        sesionDispositivoService.registrarSesion(usuario.getEmail(), tipoDispositivo);
        String token = jwtUtil.generarToken(usuario.getEmail(), roles, tipoDispositivo);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "id", usuario.getId(),
                "nombre", usuario.getNombre(),
                "email", usuario.getEmail(),
                "rol", roles,
                "dispositivo", tipoDispositivo.name()
        ));
    }

    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registro(@Valid @RequestBody RegistroRequest request) {
        if (usuarioService.existeEmail(request.getEmail()))
            return ResponseEntity.badRequest().body(Map.of("error", "El email ya está registrado"));
        Usuario nuevo = usuarioService.registrar(request.getNombre(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(Map.of(
                "mensaje", "Usuario registrado correctamente",
                "id", nuevo.getId(),
                "nombre", nuevo.getNombre(),
                "email", nuevo.getEmail()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extraerEmail(token);
        TipoDispositivo tipoDispositivo = jwtUtil.extraerTipoDispositivo(token);
        sesionDispositivoService.cerrarSesion(email, tipoDispositivo);
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente"));
    }

    @GetMapping("/verificar")
    public ResponseEntity<?> verificar() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/dispositivo/desconectar")
    public ResponseEntity<Map<String, Object>> desconectarDispositivo(
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (body != null && body.containsKey("token")) {
            token = body.get("token");
        }
        if (token == null) return ResponseEntity.status(401).build();

        String email = jwtUtil.extraerEmail(token);
        TipoDispositivo tipoDispositivo = jwtUtil.extraerTipoDispositivo(token);

        sesionDispositivoService.cerrarSesion(email, tipoDispositivo);

        List<SesionDispositivo> restantes = sesionDispositivoService
            .obtenerSesionesPorEmail(email)
            .stream()
            .filter(SesionDispositivo::getDispositivoActivo)
            .collect(Collectors.toList());

        List<String> dispositivosReproduciendo;
        if (!restantes.isEmpty()) {
            String primerTipo = restantes.get(0).getDispositivo().getTipo().name();
            sesionDispositivoService.actualizarReproduciendo(email, List.of(primerTipo));
            dispositivosReproduciendo = List.of(primerTipo); // ← antes: filtraba restantes stale
        } else {
            dispositivosReproduciendo = List.of();
        }

        ReproductorEvent evento = new ReproductorEvent();
        evento.setTipo(ReproductorEvent.Tipo.TRANSFERIR);
        evento.setUsuarioEmail(email);
        evento.setDispositivosActivos(dispositivosReproduciendo); // ← ahora sí tiene el valor correcto
        messagingTemplate.convertAndSend("/topic/reproductor/" + email, evento);

        return ResponseEntity.ok(Map.of("mensaje", "Dispositivo marcado como inactivo"));
    }
    
    
    @PostMapping("/reactivar")
    public ResponseEntity<Map<String, Object>> reactivarDispositivo(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extraerEmail(token);
        TipoDispositivo tipoDispositivo = jwtUtil.extraerTipoDispositivo(token);

        sesionDispositivoService.registrarSesion(email, tipoDispositivo);

        // Obtener el estado actualizado para devolver al front
        List<SesionDispositivo> sesiones = sesionDispositivoService.obtenerSesionesPorEmail(email);

        // Notificar a todos los dispositivos del usuario
        ReproductorEvent evento = new ReproductorEvent();
        evento.setTipo(ReproductorEvent.Tipo.TRANSFERIR);
        evento.setUsuarioEmail(email);
        List<String> reproduciendo = sesiones.stream()
            .filter(s -> Boolean.TRUE.equals(s.getReproduciendo()))
            .map(s -> s.getDispositivo().getTipo().name())
            .collect(Collectors.toList());
        evento.setDispositivosActivos(reproduciendo);
        messagingTemplate.convertAndSend("/topic/reproductor/" + email, evento);

        // ✅ Decirle al front si ESTE dispositivo debe sonar
        boolean esteReproduciendo = sesiones.stream()
            .filter(s -> s.getDispositivo().getTipo() == tipoDispositivo)
            .anyMatch(s -> Boolean.TRUE.equals(s.getReproduciendo()));

        return ResponseEntity.ok(Map.of(
            "mensaje", "Dispositivo reactivado",
            "reproduciendo", esteReproduciendo
        ));
    }
}
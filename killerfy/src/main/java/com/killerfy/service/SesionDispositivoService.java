package com.killerfy.service;

import com.killerfy.model.Dispositivo;
import com.killerfy.model.Dispositivo.TipoDispositivo;
import com.killerfy.model.SesionDispositivo;
import com.killerfy.model.Usuario;
import com.killerfy.repository.DispositivoRepository;
import com.killerfy.repository.SesionDispositivoRepository;
import com.killerfy.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SesionDispositivoService {

    private final SesionDispositivoRepository sesionRepository;
    private final DispositivoRepository dispositivoRepository;
    private final UsuarioRepository usuarioRepository;

    public SesionDispositivoService(SesionDispositivoRepository sesionRepository,
                                    DispositivoRepository dispositivoRepository,
                                    UsuarioRepository usuarioRepository) {
        this.sesionRepository = sesionRepository;
        this.dispositivoRepository = dispositivoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ─────────────────────────────────────────────────────
    // Se llama al hacer login:
    // Si ya existe sesión para ese usuario+dispositivo → activa
    // Si no existe → crea una nueva sesión activa
    // ─────────────────────────────────────────────────────
    @Transactional
    public void registrarSesion(String email, TipoDispositivo tipoDispositivo) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Dispositivo dispositivo = dispositivoRepository.findByTipo(tipoDispositivo)
            .orElseThrow(() -> new RuntimeException("Dispositivo no encontrado"));

        SesionDispositivo sesion = sesionRepository
            .findByUsuarioIdAndDispositivoId(usuario.getId(), dispositivo.getId())
            .orElse(new SesionDispositivo(usuario, dispositivo, false));

        // ✅ Solo gestiona conexión — NO toca reproduciendo
        sesion.setDispositivoActivo(true);
        sesionRepository.save(sesion);
    }

    @Transactional  // ← evita LazyInitializationException en contexto WS
    public void actualizarReproduciendo(String email, List<String> tiposReproduciendo) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        List<SesionDispositivo> sesiones = sesionRepository.findByUsuarioId(usuario.getId());
        for (SesionDispositivo sesion : sesiones) {
            boolean debeReproducir = tiposReproduciendo.contains(
                sesion.getDispositivo().getTipo().name()  // ← lazy: necesita transacción activa
            );
            sesion.setReproduciendo(debeReproducir);
            sesionRepository.save(sesion);
        }
    }

    // ─────────────────────────────────────────────────────
    // Se llama al hacer logout:
    // Pone dispositivoActivo = false para ese usuario+dispositivo
    // ─────────────────────────────────────────────────────
    @Transactional
    public void cerrarSesion(String email, TipoDispositivo tipoDispositivo) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Dispositivo dispositivo = dispositivoRepository.findByTipo(tipoDispositivo)
                .orElseThrow(() -> new RuntimeException("Dispositivo no encontrado: " + tipoDispositivo));

        sesionRepository.findByUsuarioIdAndDispositivoId(usuario.getId(), dispositivo.getId())
                .ifPresent(sesion -> {
                    sesion.setDispositivoActivo(false);
                    sesionRepository.save(sesion);
                });
    }

    // ─────────────────────────────────────────────────────
    // Devuelve todas las sesiones de un usuario (activas e inactivas)
    // Útil para mostrar en el frontend qué dispositivos tiene conectados
    // ─────────────────────────────────────────────────────
    @Transactional
    public List<SesionDispositivo> obtenerSesionesPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return sesionRepository.findByUsuarioId(usuario.getId());
    }
    
 // Actualiza qué dispositivos tienen sonido activo (puede ser varios)
    public void actualizarDispositivosSonando(String email, List<String> tiposActivos) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<SesionDispositivo> sesiones = sesionRepository.findByUsuarioId(usuario.getId());
        for (SesionDispositivo sesion : sesiones) {
            boolean debesonar = tiposActivos.contains(
                sesion.getDispositivo().getTipo().name()
            );
            sesion.setDispositivoActivo(debesonar);
            sesionRepository.save(sesion);
        }
    }
    
    
}
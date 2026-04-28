package com.killerfy.service;

import com.killerfy.model.Dispositivo;
import com.killerfy.model.Dispositivo.TipoDispositivo;
import com.killerfy.model.SesionDispositivo;
import com.killerfy.model.Usuario;
import com.killerfy.repository.DispositivoRepository;
import com.killerfy.repository.SesionDispositivoRepository;
import com.killerfy.repository.UsuarioRepository;
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
    public void registrarSesion(String email, TipoDispositivo tipoDispositivo) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Dispositivo dispositivo = dispositivoRepository.findByTipo(tipoDispositivo)
                .orElseThrow(() -> new RuntimeException("Dispositivo no encontrado: " + tipoDispositivo));

        SesionDispositivo sesion = sesionRepository
                .findByUsuarioIdAndDispositivoId(usuario.getId(), dispositivo.getId())
                .orElse(new SesionDispositivo(usuario, dispositivo, false));

        sesion.setDispositivoActivo(true);
        sesionRepository.save(sesion);
    }

    // ─────────────────────────────────────────────────────
    // Se llama al hacer logout:
    // Pone dispositivoActivo = false para ese usuario+dispositivo
    // ─────────────────────────────────────────────────────
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
    public List<SesionDispositivo> obtenerSesionesPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return sesionRepository.findByUsuarioId(usuario.getId());
    }
}
package com.killerfy.repository;

import com.killerfy.model.SesionDispositivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SesionDispositivoRepository extends JpaRepository<SesionDispositivo, Long> {

    // Todas las sesiones de un usuario
    List<SesionDispositivo> findByUsuarioId(Long usuarioId);

    // Sesión activa/inactiva de un usuario
    Optional<SesionDispositivo> findByUsuarioIdAndDispositivoActivo(Long usuarioId, Boolean activo);

    // Sesión de un usuario en un dispositivo concreto (para login/logout)
    Optional<SesionDispositivo> findByUsuarioIdAndDispositivoId(Long usuarioId, Long dispositivoId);
}
package com.killerfy.repository;

import com.killerfy.model.SesionDispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SesionDispositivoRepository extends JpaRepository<SesionDispositivo, Long> {
    List<SesionDispositivo> findByUsuarioId(Long usuarioId);
    Optional<SesionDispositivo> findByUsuarioIdAndDispositivoActivo(Long usuarioId, Boolean activo);
}
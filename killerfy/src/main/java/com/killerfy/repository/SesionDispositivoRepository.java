package com.killerfy.repository;

import com.killerfy.model.SesionDispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SesionDispositivoRepository extends JpaRepository<SesionDispositivo, Long> {

    
    @Query("SELECT s FROM SesionDispositivo s JOIN FETCH s.dispositivo WHERE s.usuario.id = :usuarioId")
    List<SesionDispositivo> findByUsuarioId(Long usuarioId);

    Optional<SesionDispositivo> findByUsuarioIdAndDispositivoActivo(Long usuarioId, Boolean activo);

    Optional<SesionDispositivo> findByUsuarioIdAndDispositivoId(Long usuarioId, Long dispositivoId);
}
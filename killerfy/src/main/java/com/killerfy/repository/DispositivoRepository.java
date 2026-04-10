package com.killerfy.repository;

import com.killerfy.model.Dispositivo;
import com.killerfy.model.Dispositivo.TipoDispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DispositivoRepository extends JpaRepository<Dispositivo, Long> {
    Optional<Dispositivo> findByTipo(TipoDispositivo tipo);
}
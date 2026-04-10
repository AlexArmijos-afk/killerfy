package com.killerfy.repository;

import com.killerfy.model.Rol;
import com.killerfy.model.Rol.NombreRol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombreRol(NombreRol nombreRol);
}
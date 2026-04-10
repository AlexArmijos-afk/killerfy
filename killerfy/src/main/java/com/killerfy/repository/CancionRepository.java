package com.killerfy.repository;

import com.killerfy.model.Cancion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CancionRepository extends JpaRepository<Cancion, Long> {
    List<Cancion> findByArtistaContainingIgnoreCase(String artista);
    List<Cancion> findByTituloContainingIgnoreCase(String titulo);
    List<Cancion> findByAlbumContainingIgnoreCase(String album);
}
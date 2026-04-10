package com.killerfy.service;

import com.killerfy.model.Cancion;
import com.killerfy.repository.CancionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CancionService {

    private final CancionRepository cancionRepository;

    public CancionService(CancionRepository cancionRepository) {
        this.cancionRepository = cancionRepository;
    }

    public List<Cancion> obtenerTodas() {
        return cancionRepository.findAll();
    }

    public Optional<Cancion> obtenerPorId(Long id) {
        return cancionRepository.findById(id);
    }

    public List<Cancion> buscarPorArtista(String artista) {
        return cancionRepository.findByArtistaContainingIgnoreCase(artista);
    }

    public List<Cancion> buscarPorTitulo(String titulo) {
        return cancionRepository.findByTituloContainingIgnoreCase(titulo);
    }

    public Cancion guardar(Cancion cancion) {
        return cancionRepository.save(cancion);
    }

    public void eliminar(Long id) {
        cancionRepository.deleteById(id);
    }
}
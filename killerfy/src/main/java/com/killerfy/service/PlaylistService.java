package com.killerfy.service;

import com.killerfy.model.Cancion;
import com.killerfy.model.Playlist;
import com.killerfy.model.PlaylistCancion;
import com.killerfy.model.PlaylistCancionId;
import com.killerfy.model.Usuario;
import com.killerfy.repository.CancionRepository;
import com.killerfy.repository.PlaylistCancionRepository;
import com.killerfy.repository.PlaylistRepository;
import com.killerfy.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistCancionRepository playlistCancionRepository;
    private final CancionRepository cancionRepository;
    private final UsuarioRepository usuarioRepository;

    public PlaylistService(PlaylistRepository playlistRepository,
                           PlaylistCancionRepository playlistCancionRepository,
                           CancionRepository cancionRepository,
                           UsuarioRepository usuarioRepository) {
        this.playlistRepository = playlistRepository;
        this.playlistCancionRepository = playlistCancionRepository;
        this.cancionRepository = cancionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Obtener todas las playlists de un usuario
    public List<Playlist> obtenerPorUsuario(Long usuarioId) {
        return playlistRepository.findByUsuarioId(usuarioId);
    }

    // Obtener playlist por ID
    public Optional<Playlist> obtenerPorId(Long id) {
        return playlistRepository.findById(id);
    }

    // Crear una nueva playlist
    public Playlist crear(String nombre, String descripcion, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Playlist playlist = new Playlist(nombre, descripcion, usuario);
        return playlistRepository.save(playlist);
    }

    // Añadir canción a una playlist
    public PlaylistCancion añadirCancion(Long playlistId, Long cancionId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist no encontrada"));

        Cancion cancion = cancionRepository.findById(cancionId)
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

        // Calcula el siguiente orden
        List<PlaylistCancion> canciones = playlistCancionRepository
                .findByPlaylistIdOrderByOrdenAsc(playlistId);
        int siguienteOrden = canciones.size() + 1;

        PlaylistCancion pc = new PlaylistCancion(playlist, cancion, siguienteOrden);
        return playlistCancionRepository.save(pc);
    }

    // Eliminar canción de una playlist
    public void eliminarCancion(Long playlistId, Long cancionId) {
        PlaylistCancionId id = new PlaylistCancionId(playlistId, cancionId);
        playlistCancionRepository.deleteById(id);
    }

    // Obtener canciones de una playlist ordenadas
    public List<PlaylistCancion> obtenerCanciones(Long playlistId) {
        return playlistCancionRepository.findByPlaylistIdOrderByOrdenAsc(playlistId);
    }

    // Eliminar playlist completa
    public void eliminar(Long id) {
        playlistRepository.deleteById(id);
    }

    // Actualizar nombre y descripción
    public Optional<Playlist> actualizar(Long id, String nombre, String descripcion) {
        return playlistRepository.findById(id).map(p -> {
            p.setNombre(nombre);
            p.setDescripcion(descripcion);
            return playlistRepository.save(p);
        });
    }
}
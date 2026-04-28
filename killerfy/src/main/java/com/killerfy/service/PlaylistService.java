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

	public PlaylistService(PlaylistRepository playlistRepository, PlaylistCancionRepository playlistCancionRepository,
			CancionRepository cancionRepository, UsuarioRepository usuarioRepository) {
		this.playlistRepository = playlistRepository;
		this.playlistCancionRepository = playlistCancionRepository;
		this.cancionRepository = cancionRepository;
		this.usuarioRepository = usuarioRepository;
	}

	// ─────────────────────────────────────────────────────
	// Obtener las playlists del usuario autenticado (por email del token)
	// ─────────────────────────────────────────────────────
	public List<Playlist> obtenerPorEmail(String email) {
		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
		return playlistRepository.findByUsuarioId(usuario.getId());
	}

	// ─────────────────────────────────────────────────────
    // Obtener playlist por ID
    // ─────────────────────────────────────────────────────
	public Optional<Playlist> obtenerPorId(Long id) {
		return playlistRepository.findById(id);
	}

	// ─────────────────────────────────────────────────────
	// Crear playlist — el dueño se busca por email del token
	// ─────────────────────────────────────────────────────
	public Playlist crear(String nombre, String descripcion, String email) {
		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		Playlist playlist = new Playlist(nombre, descripcion, usuario);
		return playlistRepository.save(playlist);
	}

	// ─────────────────────────────────────────────────────
	// Añadir canción — solo si el email del token coincide con el dueño
	// ─────────────────────────────────────────────────────
	public PlaylistCancion añadirCancion(Long playlistId, Long cancionId, String email) {
		Playlist playlist = playlistRepository.findById(playlistId)
				.orElseThrow(() -> new RuntimeException("Playlist no encontrada"));
		verificarPropietario(playlist, email);

		Cancion cancion = cancionRepository.findById(cancionId)
				.orElseThrow(() -> new RuntimeException("Canción no encontrada"));

		List<PlaylistCancion> canciones = playlistCancionRepository.findByPlaylistIdOrderByOrdenAsc(playlistId);
		int siguienteOrden = canciones.size() + 1;

		PlaylistCancion pc = new PlaylistCancion(playlist, cancion, siguienteOrden);
		return playlistCancionRepository.save(pc);
	}

	// ─────────────────────────────────────────────────────
	// Eliminar canción — solo si el email del token coincide con el dueño
	// ─────────────────────────────────────────────────────
	public void eliminarCancion(Long playlistId, Long cancionId, String email) {
		Playlist playlist = playlistRepository.findById(playlistId)
				.orElseThrow(() -> new RuntimeException("Playlist no encontrada"));
		verificarPropietario(playlist, email);

		PlaylistCancionId id = new PlaylistCancionId(playlistId, cancionId);
		playlistCancionRepository.deleteById(id);
	}

	// ─────────────────────────────────────────────────────
	// Obtener canciones de una playlist ordenadas
	// ─────────────────────────────────────────────────────
	public List<PlaylistCancion> obtenerCanciones(Long playlistId) {
		return playlistCancionRepository.findByPlaylistIdOrderByOrdenAsc(playlistId);
	}

	// ─────────────────────────────────────────────────────
	// Eliminar playlist — solo si el email del token coincide con el dueño
	// ─────────────────────────────────────────────────────
	public void eliminar(Long id, String email) {
		Playlist playlist = playlistRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Playlist no encontrada"));
		verificarPropietario(playlist, email);
		playlistRepository.deleteById(id);
	}

	// ─────────────────────────────────────────────────────
	// Actualizar — solo si el email del token coincide con el dueño
	// ─────────────────────────────────────────────────────
	public Optional<Playlist> actualizar(Long id, String email, String nombre, String descripcion) {
		return playlistRepository.findById(id).map(p -> {
			verificarPropietario(p, email);
			p.setNombre(nombre);
			p.setDescripcion(descripcion);
			return playlistRepository.save(p);
		});
	}

	// ─────────────────────────────────────────────────────
	// Método privado: verifica que el usuario autenticado es el dueño
	// Lanza SecurityException si no coincide (el controller devuelve 403)
	// ─────────────────────────────────────────────────────
	private void verificarPropietario(Playlist playlist, String email) {
		if (!playlist.getUsuario().getEmail().equals(email)) {
			throw new SecurityException("No tienes permisos para modificar esta playlist");
		}
	}

}
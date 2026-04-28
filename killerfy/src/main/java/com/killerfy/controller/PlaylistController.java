package com.killerfy.controller;

import com.killerfy.model.Playlist;
import com.killerfy.model.PlaylistCancion;
import com.killerfy.service.PlaylistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

	private final PlaylistService playlistService;

	public PlaylistController(PlaylistService playlistService) {
		this.playlistService = playlistService;
	}

	// ─────────────────────────────────────────────────────
	// GET /api/playlists/mis-playlists
	// Devuelve las playlists del usuario autenticado (email del token)
	// ─────────────────────────────────────────────────────
	@GetMapping("/mis-playlists")
	public List<Playlist> obtenerMisPlaylists() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		return playlistService.obtenerPorEmail(email);
	}

	// ─────────────────────────────────────────────────────
	// GET /api/playlists/{id}
	// ─────────────────────────────────────────────────────
	@GetMapping("/{id}")
	public ResponseEntity<Playlist> obtenerPorId(@PathVariable Long id) {
		return playlistService.obtenerPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	// ─────────────────────────────────────────────────────
	// POST /api/playlists
	// El dueño se toma del token, no del body
	// ─────────────────────────────────────────────────────
	@PostMapping
	public ResponseEntity<Playlist> crear(@RequestBody Map<String, Object> body) {
		String nombre = (String) body.get("nombre");
		String descripcion = (String) body.get("descripcion");
		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		Playlist nueva = playlistService.crear(nombre, descripcion, email);
		return ResponseEntity.ok(nueva);
	}

	// ─────────────────────────────────────────────────────
	// POST /api/playlists/{playlistId}/canciones/{cancionId}
	// Solo el dueño puede añadir canciones a su playlist
	// ─────────────────────────────────────────────────────
	@PostMapping("/{playlistId}/canciones/{cancionId}")
	public ResponseEntity<?> añadirCancion(@PathVariable Long playlistId, @PathVariable Long cancionId) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		try {
			PlaylistCancion pc = playlistService.añadirCancion(playlistId, cancionId, email);
			return ResponseEntity.ok(Map.of("mensaje", "Canción añadida correctamente", "orden", pc.getOrden()));
		} catch (SecurityException e) {
			return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
		}
	}

	// ─────────────────────────────────────────────────────
	// GET /api/playlists/{playlistId}/canciones
	// ─────────────────────────────────────────────────────
	@GetMapping("/{playlistId}/canciones")
	public List<PlaylistCancion> obtenerCanciones(@PathVariable Long playlistId) {
		return playlistService.obtenerCanciones(playlistId);
	}

	// ─────────────────────────────────────────────────────
    // DELETE /api/playlists/{playlistId}/canciones/{cancionId}
    // Solo el dueño puede eliminar canciones de su playlist
    // ─────────────────────────────────────────────────────
    @DeleteMapping("/{playlistId}/canciones/{cancionId}")
    public ResponseEntity<?> eliminarCancion(@PathVariable Long playlistId,
                                             @PathVariable Long cancionId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            playlistService.eliminarCancion(playlistId, cancionId, email);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

	// ─────────────────────────────────────────────────────
	// PUT /api/playlists/{id}
	// Solo el dueño puede editar
	// ─────────────────────────────────────────────────────
	@PutMapping("/{id}")
	public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		String nombre = (String) body.get("nombre");
		String descripcion = (String) body.get("descripcion");

		try {
			return playlistService.actualizar(id, email, nombre, descripcion).map(ResponseEntity::ok)
					.orElse(ResponseEntity.notFound().build());
		} catch (SecurityException e) {
			return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
		}
	}

	// ─────────────────────────────────────────────────────
	// DELETE /api/playlists/{id}
	// Solo el dueño puede eliminar
	// ─────────────────────────────────────────────────────
	@DeleteMapping("/{id}")
	public ResponseEntity<?> eliminar(@PathVariable Long id) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		try {
			playlistService.eliminar(id, email);
			return ResponseEntity.noContent().build();
		} catch (SecurityException e) {
			return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
		}
	}
}
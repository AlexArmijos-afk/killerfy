package com.killerfy.controller;

import com.killerfy.model.Playlist;
import com.killerfy.model.PlaylistCancion;
import com.killerfy.service.PlaylistService;
import org.springframework.http.ResponseEntity;
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

    // GET /api/playlists/usuario/1
    @GetMapping("/usuario/{usuarioId}")
    public List<Playlist> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return playlistService.obtenerPorUsuario(usuarioId);
    }

    // GET /api/playlists/1
    @GetMapping("/{id}")
    public ResponseEntity<Playlist> obtenerPorId(@PathVariable Long id) {
        return playlistService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/playlists
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        String nombre = (String) body.get("nombre");
        String descripcion = (String) body.get("descripcion");
        Long usuarioId = Long.valueOf(body.get("usuarioId").toString());

        Playlist nueva = playlistService.crear(nombre, descripcion, usuarioId);
        return ResponseEntity.ok(nueva);
    }

    // POST /api/playlists/1/canciones/1
    @PostMapping("/{playlistId}/canciones/{cancionId}")
    public ResponseEntity<?> añadirCancion(@PathVariable Long playlistId,
                                            @PathVariable Long cancionId) {
        PlaylistCancion pc = playlistService.añadirCancion(playlistId, cancionId);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Canción añadida correctamente",
                "orden", pc.getOrden()
        ));
    }

    // GET /api/playlists/1/canciones
    @GetMapping("/{playlistId}/canciones")
    public List<PlaylistCancion> obtenerCanciones(@PathVariable Long playlistId) {
        return playlistService.obtenerCanciones(playlistId);
    }

    // DELETE /api/playlists/1/canciones/1
    @DeleteMapping("/{playlistId}/canciones/{cancionId}")
    public ResponseEntity<Void> eliminarCancion(@PathVariable Long playlistId,
                                                 @PathVariable Long cancionId) {
        playlistService.eliminarCancion(playlistId, cancionId);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/playlists/1
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                         @RequestBody Map<String, String> body) {
        return playlistService.actualizar(id, body.get("nombre"), body.get("descripcion"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/playlists/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        playlistService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
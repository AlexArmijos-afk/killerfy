package com.killerfy.controller;

import com.killerfy.model.Cancion;
import com.killerfy.service.CancionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/canciones")
public class CancionController {

    private final CancionService cancionService;

    public CancionController(CancionService cancionService) {
        this.cancionService = cancionService;
    }

    @GetMapping
    public List<Cancion> obtenerTodas() {
        return cancionService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cancion> obtenerPorId(@PathVariable Long id) {
        return cancionService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public List<Cancion> buscar(@RequestParam(required = false) String titulo,
                                 @RequestParam(required = false) String artista) {
        if (titulo != null) return cancionService.buscarPorTitulo(titulo);
        if (artista != null) return cancionService.buscarPorArtista(artista);
        return cancionService.obtenerTodas();
    }

 // Solo ADMIN puede crear, editar o eliminar canciones
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Cancion> crear(@RequestBody Cancion cancion) {
        return ResponseEntity.ok(cancionService.guardar(cancion));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Cancion> actualizar(@PathVariable Long id, @RequestBody Cancion datos) {
        return cancionService.obtenerPorId(id).map(c -> {
            c.setTitulo(datos.getTitulo());
            c.setArtista(datos.getArtista());
            c.setAlbum(datos.getAlbum());
            c.setDuracionSegundos(datos.getDuracionSegundos());
            c.setUrlAudio(datos.getUrlAudio());
            return ResponseEntity.ok(cancionService.guardar(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cancionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
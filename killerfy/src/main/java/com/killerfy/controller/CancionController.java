package com.killerfy.controller;

import com.killerfy.model.Cancion;
import com.killerfy.service.CancionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/canciones")
@CrossOrigin(origins = "*") // permite peticiones desde Ionic en desarrollo
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

    @PostMapping
    public Cancion crear(@RequestBody Cancion cancion) {
        return cancionService.guardar(cancion);
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cancionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
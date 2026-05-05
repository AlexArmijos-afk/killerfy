package com.killerfy.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.killerfy.service.CancionService;
import com.killerfy.model.Cancion;

import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/canciones")
public class CancionController {

    private final CancionService cancionService;
 // ─── Inyectar ruta de uploads desde application.properties ───
    @Value("${killerfy.uploads.dir:uploads/canciones}")
    private String uploadsDir;

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
    @PutMapping("/{id}")
    public ResponseEntity<Cancion> actualizar(@PathVariable Long id, @RequestBody Cancion datos) {
        return cancionService.obtenerPorId(id).map(c -> {
            c.setTitulo(datos.getTitulo());
            c.setArtista(datos.getArtista());
            c.setAlbum(datos.getAlbum());
            c.setDuracionSegundos(datos.getDuracionSegundos());
            return ResponseEntity.ok(cancionService.guardar(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cancionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    
 // ─────────────────────────────────────────────────────────────
 // GET /api/canciones/{id}/stream
 // Sirve el archivo de audio con soporte de Range requests
 // ─────────────────────────────────────────────────────────────
 @GetMapping("/{id}/stream")
 public ResponseEntity<Resource> stream(
         @PathVariable Long id,
         @RequestHeader(value = "Range", required = false) String rangeHeader) {

	 Cancion cancion = cancionService.obtenerPorId(id)
         .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Canción no encontrada"));

     try {
         Path ruta     = Paths.get(uploadsDir).resolve(cancion.getNombreArchivo()).normalize();
         Resource res  = new UrlResource(ruta.toUri());

         if (!res.exists() || !res.isReadable()) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Archivo no encontrado");
         }

         long fileSize = Files.size(ruta);
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
         headers.add("Accept-Ranges", "bytes");
         headers.add("Access-Control-Allow-Origin", "*");
         headers.add("Access-Control-Expose-Headers", "Content-Range, Accept-Ranges, Content-Length");

         // ── Con Range header (seek del reproductor) ──────────
         if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
             String[] parts = rangeHeader.substring(6).split("-");
             long start = Long.parseLong(parts[0]);
             long end   = (parts.length > 1 && !parts[1].isEmpty())
                        ? Long.parseLong(parts[1])
                        : fileSize - 1;

             // Validar rango
             if (start > end || start >= fileSize) {
                 return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                         .header("Content-Range", "bytes */" + fileSize)
                         .build();
             }
             end = Math.min(end, fileSize - 1);

             long contentLength = end - start + 1;
             byte[] data  = Files.readAllBytes(ruta);
             byte[] chunk = new byte[(int) contentLength];
             System.arraycopy(data, (int) start, chunk, 0, (int) contentLength);

             headers.add("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
             headers.setContentLength(contentLength);

             return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                     .headers(headers)
                     .body(new ByteArrayResource(chunk));
         }

         // ── Sin Range: archivo completo ───────────────────────
         headers.setContentLength(fileSize);
         return ResponseEntity.ok()
                 .headers(headers)
                 .body(res);

     } catch (ResponseStatusException e) {
         throw e;
     } catch (Exception e) {
         throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al servir el audio");
     }
 }
 
//─────────────────────────────────────────────────────────────
//POST /api/canciones  (solo ADMIN)
//Sube un archivo MP3 y crea la canción en BD
//─────────────────────────────────────────────────────────────
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Cancion> crear(
      @RequestParam("titulo")    String titulo,
      @RequestParam("artista")   String artista,
      @RequestParam("album")     String album,
      @RequestParam("duracion")  int duracionSegundos,
      @RequestParam("archivo")   MultipartFile archivo) {
  try {
      // Guardar archivo en disco
      String nombreArchivo = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
      Path destino = Paths.get(uploadsDir).resolve(nombreArchivo);
      Files.createDirectories(destino.getParent());
      Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

      // Guardar en BD
      Cancion cancion = new Cancion();
      cancion.setTitulo(titulo);
      cancion.setArtista(artista);
      cancion.setAlbum(album);
      cancion.setDuracionSegundos(duracionSegundos);
      cancion.setNombreArchivo(nombreArchivo);

      return ResponseEntity.ok(cancionService.guardar(cancion));
  } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al subir la canción");
  }
} 
}
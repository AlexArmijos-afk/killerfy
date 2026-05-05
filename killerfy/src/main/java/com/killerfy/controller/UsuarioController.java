package com.killerfy.controller;

import com.killerfy.model.Usuario;
import com.killerfy.service.SesionDispositivoService;
import com.killerfy.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SesionDispositivoService sesionDispositivoService;

    public UsuarioController(UsuarioService usuarioService,
                             SesionDispositivoService sesionDispositivoService) {
        this.usuarioService = usuarioService;
        this.sesionDispositivoService = sesionDispositivoService;
    }

    // ─────────────────────────────────────────────────────
    // GET /api/usuarios/perfil
    // Devuelve el perfil del usuario autenticado
    // ─────────────────────────────────────────────────────
    @GetMapping("/perfil")
    public ResponseEntity<?> obtenerPerfil() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─────────────────────────────────────────────────────
    // PUT /api/usuarios/perfil
    // Edita nombre y avatar del usuario autenticado
    // ─────────────────────────────────────────────────────
    @PutMapping("/perfil")
    public ResponseEntity<?> actualizarPerfil(@RequestBody Map<String, String> body) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String nuevoNombre = body.get("nombre");
        String nuevoAvatar = body.get("avatar");

        try {
            Usuario actualizado = usuarioService.actualizarPerfil(email, nuevoNombre, nuevoAvatar);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────
    // PUT /api/usuarios/cambiar-password
    // Cambia la contraseña del usuario autenticado
    // ─────────────────────────────────────────────────────
    @PutMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, String> body) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String passwordActual = body.get("passwordActual");
        String passwordNueva = body.get("passwordNueva");

        try {
            usuarioService.cambiarPassword(email, passwordActual, passwordNueva);
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
 // ─────────────────────────────────────────────────────
 // PUT /api/usuarios/admin/{id}/rol (solo ADMIN)
 // Cambia el rol de un usuario
 // ─────────────────────────────────────────────────────
 @PreAuthorize("hasRole('ADMIN')")
 @PutMapping("/admin/{id}/rol")
 public ResponseEntity<?> cambiarRol(@PathVariable Long id,
                                      @RequestBody Map<String, String> body) {
     try {
         Usuario actualizado = usuarioService.cambiarRol(id, body.get("rol"));
         return ResponseEntity.ok(actualizado);
     } catch (RuntimeException e) {
         return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
     }
 }

    // ─────────────────────────────────────────────────────
    // GET /api/usuarios/mis-dispositivos
    // Lista los dispositivos y su estado activo/inactivo
    // ─────────────────────────────────────────────────────
    @GetMapping("/mis-dispositivos")
    public ResponseEntity<?> obtenerMisDispositivos() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(sesionDispositivoService.obtenerSesionesPorEmail(email));
    }

    // ─────────────────────────────────────────────────────
    // GET /api/admin/usuarios  (solo ADMIN)
    // Lista todos los usuarios registrados
    // ─────────────────────────────────────────────────────
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/todos")
    public List<Usuario> listarTodos() {
        return usuarioService.listarTodos();
    }

    // ─────────────────────────────────────────────────────
    // DELETE /api/admin/usuarios/{id}  (solo ADMIN)
    // Elimina un usuario por ID
    // ─────────────────────────────────────────────────────
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            usuarioService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
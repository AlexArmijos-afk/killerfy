package com.killerfy.controller;

import com.killerfy.dto.LoginRequest;
import com.killerfy.dto.RegistroRequest;
import com.killerfy.model.Usuario;
import com.killerfy.security.JwtUtil;
import com.killerfy.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	// Atributos
	private final UsuarioService usuarioService;
	private final JwtUtil jwtUtil;

	// Constructor
	public AuthController(UsuarioService usuarioService, JwtUtil jwtUtil) {
	    this.usuarioService = usuarioService;
	    this.jwtUtil = jwtUtil;
	}

	// Método login — solo cambia el return final
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
	    Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(request.getEmail());

	    if (usuarioOpt.isEmpty()) {
	        return ResponseEntity.status(401)
	                .body(Map.of("error", "Email o contraseña incorrectos"));
	    }

	    Usuario usuario = usuarioOpt.get();

	    if (!usuarioService.verificarPassword(request.getPassword(), usuario.getPassword())) {
	        return ResponseEntity.status(401)
	                .body(Map.of("error", "Email o contraseña incorrectos"));
	    }

	    // ✅ Genera el token y lo devuelve
	    String token = jwtUtil.generarToken(
	    	    usuario.getEmail(),
	    	    usuario.getRoles().iterator().next().getNombreRol().name()
	    	    // .name() convierte el enum a String → "USER" o "ADMIN"
	    	);

	    	return ResponseEntity.ok(Map.of(
	    	    "token",  token,
	    	    "id",     usuario.getId(),
	    	    "nombre", usuario.getNombre(),
	    	    "email",  usuario.getEmail(),
	    	    "rol",    usuario.getRoles().iterator().next().getNombreRol().name()
	    	));
	}

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody RegistroRequest request) {
        if (usuarioService.existeEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El email ya está registrado"));
        }

        Usuario nuevo = usuarioService.registrar(
                request.getNombre(),
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity.ok(Map.of(
                "mensaje", "Usuario registrado correctamente",
                "id", nuevo.getId(),
                "nombre", nuevo.getNombre(),
                "email", nuevo.getEmail()
        ));
    }

    
}
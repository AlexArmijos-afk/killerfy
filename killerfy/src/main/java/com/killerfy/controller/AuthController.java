package com.killerfy.controller;

import com.killerfy.dto.LoginRequest;
import com.killerfy.dto.RegistroRequest;
import com.killerfy.model.Dispositivo.TipoDispositivo;
import com.killerfy.model.Usuario;
import com.killerfy.security.JwtUtil;
import com.killerfy.service.SesionDispositivoService;
import com.killerfy.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	// Atributos
	private final UsuarioService usuarioService;
	private final JwtUtil jwtUtil;
	private final SesionDispositivoService sesionDispositivoService;

	// Constructor
	public AuthController(UsuarioService usuarioService, JwtUtil jwtUtil,
			SesionDispositivoService sesionDispositivoService) {
		this.usuarioService = usuarioService;
		this.jwtUtil = jwtUtil;
		this.sesionDispositivoService = sesionDispositivoService;
	}

	// ─────────────────────────────────────────────────────
	// POST /api/auth/login
	// Body: { email, password, tipoDispositivo: "WEB" | "ANDROID" | "DESKTOP" }
	// ─────────────────────────────────────────────────────
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
		Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(request.getEmail());

		if (usuarioOpt.isEmpty()) {
			return ResponseEntity.status(401).body(Map.of("error", "Email o contraseña incorrectos"));
		}

		Usuario usuario = usuarioOpt.get();

		if (!usuarioService.verificarPassword(request.getPassword(), usuario.getPassword())) {
			return ResponseEntity.status(401).body(Map.of("error", "Email o contraseña incorrectos"));
		}

		List<String> roles = usuario.getRoles().stream()
				.map(r -> r.getNombreRol().name())
				.collect(java.util.stream.Collectors.toList());
		TipoDispositivo tipoDispositivo = request.getTipoDispositivo();

		// Registra o activa la sesión del dispositivo
		sesionDispositivoService.registrarSesion(usuario.getEmail(), tipoDispositivo);

		// Genera el token incluyendo el dispositivo
			String token = jwtUtil.generarToken(usuario.getEmail(), roles, tipoDispositivo);

		return ResponseEntity.ok(Map.of("token", token, "id", usuario.getId(), "nombre", usuario.getNombre(), "email",
				usuario.getEmail(), "rol", roles, "dispositivo", tipoDispositivo.name()));
	}

	// ─────────────────────────────────────────────────────
	// POST /api/auth/registro
	// Body: { nombre, email, password }
	// ─────────────────────────────────────────────────────
	@PostMapping("/registro")
	public ResponseEntity<Map<String, Object>> registro(@Valid @RequestBody RegistroRequest request) {
		if (usuarioService.existeEmail(request.getEmail())) {
			return ResponseEntity.badRequest().body(Map.of("error", "El email ya está registrado"));
		}

		Usuario nuevo = usuarioService.registrar(request.getNombre(), request.getEmail(), request.getPassword());

		return ResponseEntity.ok(Map.of("mensaje", "Usuario registrado correctamente", "id", nuevo.getId(), "nombre",
				nuevo.getNombre(), "email", nuevo.getEmail()));
	}

	// ─────────────────────────────────────────────────────
	// POST /api/auth/logout
	// Extrae el email y dispositivo del token y pone dispositivoActivo = false
	// No necesita body — toda la info viene en el JWT del header
	// ─────────────────────────────────────────────────────
	@PostMapping("/logout")
	public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String authHeader) {

		String token = authHeader.substring(7);
		String email = jwtUtil.extraerEmail(token);
		TipoDispositivo tipoDispositivo = jwtUtil.extraerTipoDispositivo(token);

		sesionDispositivoService.cerrarSesion(email, tipoDispositivo);

		return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente"));
	}

}
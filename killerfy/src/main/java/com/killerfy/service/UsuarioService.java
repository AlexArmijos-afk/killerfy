package com.killerfy.service;

import com.killerfy.model.Rol;
import com.killerfy.model.Rol.NombreRol;
import com.killerfy.model.Usuario;
import com.killerfy.repository.RolRepository;
import com.killerfy.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final RolRepository rolRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
			PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.rolRepository = rolRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public boolean existeEmail(String email) {
		return usuarioRepository.existsByEmail(email);
	}

	// ─────────────────────────────────────────────────────
	// Registro — el rol USER ya existe gracias a DataInitializer
	// ─────────────────────────────────────────────────────
	public Usuario registrar(String nombre, String email, String password) {
		String passwordEncriptada = passwordEncoder.encode(password);

		Rol rolUser = rolRepository.findByNombreRol(NombreRol.USER)
				.orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));

		Set<Rol> roles = new HashSet<>();
		roles.add(rolUser);

		Usuario usuario = new Usuario(nombre, email, passwordEncriptada);
		usuario.setRoles(roles);

		return usuarioRepository.save(usuario);
	}

	public Optional<Usuario> buscarPorEmail(String email) {
		return usuarioRepository.findByEmail(email);
	}

	public boolean verificarPassword(String rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword, encodedPassword);
	}
	
	// ─────────────────────────────────────────────────────
    // Actualizar nombre y avatar del perfil
    // ─────────────────────────────────────────────────────
    public Usuario actualizarPerfil(String email, String nuevoNombre, String nuevoAvatar) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (nuevoNombre != null && !nuevoNombre.isBlank()) {
            usuario.setNombre(nuevoNombre);
        }
        if (nuevoAvatar != null && !nuevoAvatar.isBlank()) {
            usuario.setAvatar(nuevoAvatar);
        }

        return usuarioRepository.save(usuario);
    }
    
 // ─────────────────────────────────────────────────────
    // Cambiar contraseña — verifica la actual antes de cambiar
    // ─────────────────────────────────────────────────────
    public void cambiarPassword(String email, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new RuntimeException("La contraseña actual no es correcta");
        }
        if (passwordNueva == null || passwordNueva.length() < 6) {
            throw new RuntimeException("La nueva contraseña debe tener al menos 6 caracteres");
        }

        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
    }
    
 // ─────────────────────────────────────────────────────
    // Admin — listar todos los usuarios
    // ─────────────────────────────────────────────────────
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }
    
 // ─────────────────────────────────────────────────────
    // Admin — eliminar usuario por ID
    // ─────────────────────────────────────────────────────
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }
    
 // ─────────────────────────────────────────────────────
 // Admin — cambiar rol de un usuario
 // ─────────────────────────────────────────────────────
 public Usuario cambiarRol(Long id, String nuevoRol) {
     Usuario usuario = usuarioRepository.findById(id)
         .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

     Rol rol = rolRepository.findByNombreRol(NombreRol.valueOf(nuevoRol))
         .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + nuevoRol));

     usuario.getRoles().clear();
     usuario.getRoles().add(rol);
     return usuarioRepository.save(usuario);
 }
}
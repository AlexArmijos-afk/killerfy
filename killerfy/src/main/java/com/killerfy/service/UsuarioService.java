package com.killerfy.service;

import com.killerfy.model.Rol;
import com.killerfy.model.Rol.NombreRol;
import com.killerfy.model.Usuario;
import com.killerfy.repository.RolRepository;
import com.killerfy.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public Usuario registrar(String nombre, String email, String password) {
        // Encripta la contraseña antes de guardar
        String passwordEncriptada = passwordEncoder.encode(password);

        // Asigna rol USER por defecto
        Rol rolUser = rolRepository.findByNombreRol(NombreRol.USER)
                .orElseGet(() -> rolRepository.save(new Rol(NombreRol.USER)));

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
}
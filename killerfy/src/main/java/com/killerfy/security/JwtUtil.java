package com.killerfy.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.killerfy.model.Dispositivo.TipoDispositivo;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiracion}")
	private long expiracion;

	private SecretKey getKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	// ─────────────────────────────────────────────────────
	// Genera el token incluyendo email, rol y tipoDispositivo
	// ─────────────────────────────────────────────────────
	public String generarToken(String email, List<String> rol, TipoDispositivo tipoDispositivo) {
		return Jwts.builder().subject(email).claim("rol", rol).claim("dispositivo", tipoDispositivo.name())
				.issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + expiracion)).signWith(getKey())
				.compact();
	}

	public String extraerEmail(String token) {
		return getClaims(token).getSubject();
	}

	public String extraerRol(String token) {
	    Object rol = getClaims(token).get("rol"); // ← "rol" no "roles"
	    if (rol instanceof List<?> list && !list.isEmpty()) {
	        return list.get(0).toString();
	    }
	    return rol != null ? rol.toString() : "";
	}

	public List<String> extraerRoles(String token) {
	    Object rol = getClaims(token).get("rol"); // ← "rol" no "roles"
	    if (rol instanceof List<?>) {
	        return ((List<?>) rol).stream()
	                .map(Object::toString)
	                .collect(java.util.stream.Collectors.toList());
	    }
	    
	    return rol != null ? List.of(rol.toString()) : List.of();
	}
	
	// ─────────────────────────────────────────────────────
    // Extrae el tipoDispositivo guardado en el token
    // ─────────────────────────────────────────────────────
    public TipoDispositivo extraerTipoDispositivo(String token) {
        String dispositivo = (String) getClaims(token).get("dispositivo");
        return TipoDispositivo.valueOf(dispositivo);
    }

    public boolean esValido(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
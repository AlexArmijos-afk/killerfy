package com.killerfy.dto;

import com.killerfy.model.Dispositivo.TipoDispositivo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LoginRequest {
	
	@NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;
	
	@NotBlank(message = "La contraseña es obligatoria")
    private String password;
	
	@NotNull(message = "El tipo de dispositivo es obligatorio")
    private TipoDispositivo tipoDispositivo;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public TipoDispositivo getTipoDispositivo() { return tipoDispositivo; }
    public void setTipoDispositivo(TipoDispositivo tipoDispositivo) { this.tipoDispositivo = tipoDispositivo; }
}
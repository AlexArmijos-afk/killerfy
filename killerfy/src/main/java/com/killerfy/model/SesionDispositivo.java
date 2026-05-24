package com.killerfy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sesiones_dispositivo")
public class SesionDispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private Dispositivo dispositivo;

    @Column(name = "dispositivo_activo", nullable = false)
    private Boolean dispositivoActivo = false;
    
    @Column(name = "reproduciendo", nullable = false)
    private Boolean reproduciendo = false;

    @Column(name = "fecha_ultimo_evento")
    private LocalDateTime fechaUltimoEvento;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.fechaUltimoEvento = LocalDateTime.now();
    }

    // Constructores
    public SesionDispositivo() {}

    public SesionDispositivo(Usuario usuario, Dispositivo dispositivo, Boolean dispositivoActivo) {
        this.usuario = usuario;
        this.dispositivo = dispositivo;
        this.dispositivoActivo = dispositivoActivo;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Dispositivo getDispositivo() { return dispositivo; }
    public void setDispositivo(Dispositivo dispositivo) { this.dispositivo = dispositivo; }

    public Boolean getDispositivoActivo() { return dispositivoActivo; }
    public void setDispositivoActivo(Boolean dispositivoActivo) { this.dispositivoActivo = dispositivoActivo; }

    public LocalDateTime getFechaUltimoEvento() { return fechaUltimoEvento; }
    public void setFechaUltimoEvento(LocalDateTime fechaUltimoEvento) { this.fechaUltimoEvento = fechaUltimoEvento; }
    
    public Boolean getReproduciendo() { return reproduciendo; }
    public void setReproduciendo(Boolean reproduciendo) { this.reproduciendo = reproduciendo; }
}
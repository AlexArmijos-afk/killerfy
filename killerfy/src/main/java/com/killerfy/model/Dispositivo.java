package com.killerfy.model;

import jakarta.persistence.*;

@Entity
@Table(name = "dispositivos")
public class Dispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDispositivo tipo;

    public enum TipoDispositivo {
        WEB, ANDROID, DESKTOP
    }

    // Constructores
    public Dispositivo() {}

    public Dispositivo(TipoDispositivo tipo) {
        this.tipo = tipo;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoDispositivo getTipo() { return tipo; }
    public void setTipo(TipoDispositivo tipo) { this.tipo = tipo; }
}
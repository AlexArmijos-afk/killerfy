package com.killerfy.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private NombreRol nombreRol;

    public enum NombreRol {
        USER, ADMIN
    }

    // Constructores
    public Rol() {}

    public Rol(NombreRol nombreRol) {
        this.nombreRol = nombreRol;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public NombreRol getNombreRol() { return nombreRol; }
    public void setNombreRol(NombreRol nombreRol) { this.nombreRol = nombreRol; }
}
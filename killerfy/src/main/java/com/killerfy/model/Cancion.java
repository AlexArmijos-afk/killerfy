package com.killerfy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "canciones")
public class Cancion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String artista;

    @Column(length = 150)
    private String album;

    @Column(name = "duracion_segundos")
    private Integer duracionSegundos;

    
    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    // Constructores
    public Cancion() {}

    public Cancion(String titulo, String artista, String album,
                   Integer duracionSegundos, String urlAudio) {
        this.titulo = titulo;
        this.artista = artista;
        this.album = album;
        this.duracionSegundos = duracionSegundos;
    }

    // Getters y Setters
    
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getArtista() { return artista; }
    public void setArtista(String artista) { this.artista = artista; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public Integer getDuracionSegundos() { return duracionSegundos; }
    public void setDuracionSegundos(Integer duracionSegundos) { this.duracionSegundos = duracionSegundos; }

    
}
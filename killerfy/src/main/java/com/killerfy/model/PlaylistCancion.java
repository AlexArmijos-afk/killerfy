package com.killerfy.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Table(name = "playlist_canciones")
public class PlaylistCancion {

    @EmbeddedId
    private PlaylistCancionId id = new PlaylistCancionId();

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("playlistId")
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cancionId")
    @JoinColumn(name = "cancion_id")
    private Cancion cancion;

    @Column(nullable = false)
    private Integer orden;

    // Constructores
    public PlaylistCancion() {}

    public PlaylistCancion(Playlist playlist, Cancion cancion, Integer orden) {
        this.playlist = playlist;
        this.cancion = cancion;
        this.orden = orden;
    }

    // Getters y Setters
    public PlaylistCancionId getId() { return id; }
    public void setId(PlaylistCancionId id) { this.id = id; }

    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) { this.playlist = playlist; }

    public Cancion getCancion() { return cancion; }
    public void setCancion(Cancion cancion) { this.cancion = cancion; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
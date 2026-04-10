package com.killerfy.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PlaylistCancionId implements Serializable {

    private Long playlistId;
    private Long cancionId;

    public PlaylistCancionId() {}

    public PlaylistCancionId(Long playlistId, Long cancionId) {
        this.playlistId = playlistId;
        this.cancionId = cancionId;
    }

    public Long getPlaylistId() { return playlistId; }
    public void setPlaylistId(Long playlistId) { this.playlistId = playlistId; }

    public Long getCancionId() { return cancionId; }
    public void setCancionId(Long cancionId) { this.cancionId = cancionId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaylistCancionId)) return false;
        PlaylistCancionId that = (PlaylistCancionId) o;
        return Objects.equals(playlistId, that.playlistId) &&
               Objects.equals(cancionId, that.cancionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playlistId, cancionId);
    }
}
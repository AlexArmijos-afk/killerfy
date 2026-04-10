package com.killerfy.repository;

import com.killerfy.model.PlaylistCancion;
import com.killerfy.model.PlaylistCancionId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlaylistCancionRepository extends JpaRepository<PlaylistCancion, PlaylistCancionId> {
    List<PlaylistCancion> findByPlaylistIdOrderByOrdenAsc(Long playlistId);
}
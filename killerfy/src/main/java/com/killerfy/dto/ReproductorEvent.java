package com.killerfy.dto;

import java.util.List;
import java.util.Map;

public class ReproductorEvent {

    /**
     * SEEK añadido para sincronizar la posición de reproducción entre dispositivos.
     * Sin este valor, Jackson lanza una excepción al deserializar eventos SEEK
     * provenientes del frontend y la sincronización de posición falla silenciosamente.
     */
    public enum Tipo {
        PLAY, PAUSE, SIGUIENTE, ANTERIOR, CAMBIAR_CANCION, TRANSFERIR, SEEK
    }

    private Tipo tipo;
    private Long cancionId;
    private Double progreso;
    private String dispositivo;
    private String usuarioEmail;
    private List<String> dispositivosActivos;
    private List<Map<String, Object>> cola; 

    // Constructors
    public ReproductorEvent() {}

    public ReproductorEvent(Tipo tipo, String usuarioEmail) {
        this.tipo = tipo;
        this.usuarioEmail = usuarioEmail;
    }

    // Getters y Setters
    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo t) { this.tipo = t; }
    public Long getCancionId() { return cancionId; }
    public void setCancionId(Long id) { this.cancionId = id; }
    public Double getProgreso() { return progreso; }
    public void setProgreso(Double p) { this.progreso = p; }
    public String getDispositivo() { return dispositivo; }
    public void setDispositivo(String d) { this.dispositivo = d; }
    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String e) { this.usuarioEmail = e; }
    public List<String> getDispositivosActivos() { return dispositivosActivos; }
    public void setDispositivosActivos(List<String> d) { this.dispositivosActivos = d; }
    
    public List<Map<String, Object>> getCola() { return cola; }
    public void setCola(List<Map<String, Object>> c) { this.cola = c; }
}
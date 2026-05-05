package com.killerfy.dto;

public class ReproductorEvent {

    public enum Tipo {
        PLAY, PAUSE, SIGUIENTE, ANTERIOR, CAMBIAR_CANCION, TRANSFERIR
    }

    private Tipo    tipo;
    private Long    cancionId;
    private Double  progreso;
    private String  dispositivo;
    private String  usuarioEmail;

    // Constructors
    public ReproductorEvent() {}

    public ReproductorEvent(Tipo tipo, String usuarioEmail) {
        this.tipo         = tipo;
        this.usuarioEmail = usuarioEmail;
    }

    // Getters y Setters
    public Tipo    getTipo()          { return tipo; }
    public void    setTipo(Tipo t)    { this.tipo = t; }
    public Long    getCancionId()     { return cancionId; }
    public void    setCancionId(Long id) { this.cancionId = id; }
    public Double  getProgreso()      { return progreso; }
    public void    setProgreso(Double p) { this.progreso = p; }
    public String  getDispositivo()   { return dispositivo; }
    public void    setDispositivo(String d) { this.dispositivo = d; }
    public String  getUsuarioEmail()  { return usuarioEmail; }
    public void    setUsuarioEmail(String e) { this.usuarioEmail = e; }
}
package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto;

public class PistaUpdateRequest {
    private String nombre;
    private String ubicacion;
    private Double precioHora;
    private Boolean activa;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public Double getPrecioHora() { return precioHora; }
    public void setPrecioHora(Double precioHora) { this.precioHora = precioHora; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
}


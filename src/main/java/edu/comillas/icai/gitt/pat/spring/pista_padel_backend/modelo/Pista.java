package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pistas")
public class Pista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPista;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String ubicacion;
    private Double precioHora;
    private boolean activa;
    private LocalDateTime fechaAlta;

    public Pista() {}

    // Getters y Setters
    public Long getIdPista() { return idPista; }
    public void setIdPista(Long idPista) { this.idPista = idPista; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public Double getPrecioHora() { return precioHora; }
    public void setPrecioHora(Double precioHora) { this.precioHora = precioHora; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }
}
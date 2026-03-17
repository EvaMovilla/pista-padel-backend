package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Rol;

public class MeResponse {
    private Long idUsuario;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private Rol rol;

    public MeResponse() {}

    public MeResponse(Long idUsuario, String nombre, String apellidos, String email, String telefono, Rol rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.rol = rol;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }
    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Rol getRol() {
        return rol;
    }
    public void setRol(Rol rol) {
        this.rol = rol;
    }
}


package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto;

import jakarta.validation.constraints.Email;

public class UserUpdateRequest {

    private String nombre;
    private String apellidos;
    private String telefono;

    @Email
    private String email;

    public UserUpdateRequest() {
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
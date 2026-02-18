package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(min = 1, max = 60) String nombre,
        @Size(min = 1, max = 80) String apellidos,
        @Email String email,
        @Size(min = 6, max = 30) String telefono,
        Boolean activo
) {}

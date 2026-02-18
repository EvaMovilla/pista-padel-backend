package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaUpdateRequest(
        @NotNull LocalDate fechaReserva,
        @NotNull LocalTime horaInicio,
        @Min(30) @Max(180) Integer duracionMinutos
) {}
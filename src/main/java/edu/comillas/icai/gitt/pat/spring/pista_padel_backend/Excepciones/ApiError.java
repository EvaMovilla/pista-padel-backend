package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.Excepciones;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {}

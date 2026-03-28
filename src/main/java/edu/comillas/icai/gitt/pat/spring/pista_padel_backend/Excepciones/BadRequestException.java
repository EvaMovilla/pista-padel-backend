package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.Excepciones;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}

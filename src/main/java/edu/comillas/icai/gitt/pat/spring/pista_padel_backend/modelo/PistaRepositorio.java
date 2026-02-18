package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PistaRepositorio extends JpaRepository<Pista, Long> {
    List<Pista> findByActiva(boolean activa);
    boolean existsByNombre(String nombre);
}
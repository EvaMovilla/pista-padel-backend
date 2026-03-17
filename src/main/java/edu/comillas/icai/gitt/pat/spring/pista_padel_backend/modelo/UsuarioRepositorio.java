package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    java.util.Optional<Usuario> findByEmailIgnoreCase(String email);


}

package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.PistaRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.PistaUpdateRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.EstadoReserva;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.PistaRepositorio;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.ReservaRepositorio;
import java.time.LocalDate;


@Service
public class PistaService {

    private static final Logger log = LoggerFactory.getLogger(PistaService.class);

    private final PistaRepositorio pistaRepo;
    private final ReservaRepositorio reservaRepo;

    public PistaService(PistaRepositorio pistaRepo, ReservaRepositorio reservaRepo) {
        this.pistaRepo = pistaRepo;
        this.reservaRepo = reservaRepo;
    }

    // ===== Lo que ya tenías =====
    public Pista crearPista(PistaRequest request) {
        if (pistaRepo.existsByNombre(request.getNombre())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de la pista ya existe"); // 409
        }

        Pista pista = new Pista();
        pista.setNombre(request.getNombre());
        pista.setUbicacion(request.getUbicacion());
        pista.setPrecioHora(request.getPrecioHora()); // Double
        pista.setActiva(request.getActiva() != null ? request.getActiva() : true);
        pista.setFechaAlta(LocalDateTime.now());

        return pistaRepo.save(pista);
    }

    public List<Pista> listarPistas(Boolean activa) {
        if (activa != null) return pistaRepo.findByActiva(activa);
        return pistaRepo.findAll();
    }

    public Pista obtenerPista(Long id) {
        return pistaRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada"));
    }

    public List<Pista> list() {
        return pistaRepo.findAll();
    }

    public Pista get(Long id) {
        return obtenerPista(id);
    }

    @Transactional
    public Pista patch(Long id, PistaUpdateRequest req) {
        Pista p = obtenerPista(id);

        if (req.getNombre() != null && !req.getNombre().equalsIgnoreCase(p.getNombre())) {
            if (pistaRepo.existsByNombre(req.getNombre())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Nombre de pista duplicado"); // 409
            }
            p.setNombre(req.getNombre());
        }
        if (req.getUbicacion() != null) p.setUbicacion(req.getUbicacion());
        if (req.getPrecioHora() != null) p.setPrecioHora(req.getPrecioHora()); // Double
        if (req.getActiva() != null) p.setActiva(req.getActiva());

        log.info("Pista {} actualizada", id);
        return p;
    }

    @Transactional
    public void delete(Long id) {
        Pista p = obtenerPista(id);

        // Comprobar si hay reservas futuras activas
        boolean tieneReservasFuturas = reservaRepo
                .existsByPista_IdPistaAndFechaReservaAfterAndEstado(
                        id, LocalDate.now(), EstadoReserva.ACTIVA
                );

        if (tieneReservasFuturas) {
            // Desactivación lógica en lugar de borrado físico
            p.setActiva(false);
            log.info("Pista desactivada lógicamente (tiene reservas futuras), id={}", id);
        } else {
            pistaRepo.delete(p);
            log.info("Pista eliminada físicamente, id={}", id);
        }
    }

}

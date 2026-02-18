package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.PistaRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.PistaRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PistaService {

    private final PistaRepositorio pistaRepositorio;

    public PistaService(PistaRepositorio pistaRepositorio) {
        this.pistaRepositorio = pistaRepositorio;
    }

    public Pista crearPista(PistaRequest request) {
        if (pistaRepositorio.existsByNombre(request.getNombre())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de la pista ya existe"); // 409 [cite: 160]
        }

        Pista pista = new Pista();
        pista.setNombre(request.getNombre());
        pista.setUbicacion(request.getUbicacion());
        pista.setPrecioHora(request.getPrecioHora());
        pista.setActiva(request.getActiva() != null ? request.getActiva() : true);
        pista.setFechaAlta(LocalDateTime.now());

        return pistaRepositorio.save(pista);
    }

    public List<Pista> listarPistas(Boolean activa) {
        if (activa != null) {
            return pistaRepositorio.findByActiva(activa);
        }
        return pistaRepositorio.findAll();
    }

    public Pista obtenerPista(Long id) {
        return pistaRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada")); // 404 [cite: 163]
    }

    // Aquí irían los métodos de actualizar y eliminar/desactivar siguiendo el mismo patrón...
}
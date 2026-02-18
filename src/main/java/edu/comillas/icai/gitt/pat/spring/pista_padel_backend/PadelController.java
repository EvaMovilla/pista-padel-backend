package edu.comillas.icai.gitt.pat.spring.pista_padel_backend;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.*;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.*;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/pistaPadel")
public class PadelController {

    private final PistaService pistaService;
    private final ReservaService reservaService;
    private final UsuarioRepositorio usuarioRepositorio;

    public PadelController(PistaService pistaService, ReservaService reservaService, UsuarioRepositorio usuarioRepositorio) {
        this.pistaService = pistaService;
        this.reservaService = reservaService;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    // --- ENDPOINTS DE PISTAS (/courts) ---

    @GetMapping("/courts")
    public List<Pista> getCourts(@RequestParam(required = false) Boolean activa) {
        return pistaService.listarPistas(activa);
    }

    @PostMapping("/courts")
    public ResponseEntity<Pista> createCourt(@RequestBody PistaRequest request) {
        Usuario actual = getUsuarioLogueado();
        if (actual.getRol() != Rol.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administradores pueden crear pistas");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(pistaService.crearPista(request));
    }

    // --- ENDPOINTS DE RESERVAS (/reservations) ---

    @PostMapping("/reservations")
    public ResponseEntity<Reserva> createReservation(@RequestBody ReservaRequest request) {
        Usuario actual = getUsuarioLogueado();
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.crearReserva(request, actual));
    }

    @GetMapping("/reservations")
    public List<Reserva> getMyReservations() {
        Usuario actual = getUsuarioLogueado();
        return reservaService.listarMisReservas(actual);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        Usuario actual = getUsuarioLogueado();
        reservaService.cancelarReserva(id, actual);
        return ResponseEntity.noContent().build();
    }

    // --- DISPONIBILIDAD ---

    @GetMapping("/availability")
    public List<Reserva> checkAvailability(
            @RequestParam Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reservaService.consultarDisponibilidad(courtId, date);
    }

    // Método auxiliar para obtener quién está al teclado
    private Usuario getUsuarioLogueado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
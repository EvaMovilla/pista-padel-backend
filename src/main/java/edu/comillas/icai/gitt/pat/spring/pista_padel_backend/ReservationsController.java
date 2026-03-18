package edu.comillas.icai.gitt.pat.spring.pista_padel_backend;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.ReservaRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.ReservaUpdateRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.Excepciones.NotFoundException;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.*;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio.ReservaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/pistaPadel")
public class ReservationsController {

    private final ReservaService reservaService;
    private final UsuarioRepositorio usuarioRepo;

    public ReservationsController(ReservaService reservaService, UsuarioRepositorio usuarioRepo) {
        this.reservaService = reservaService;
        this.usuarioRepo = usuarioRepo;
    }

    @PostMapping("/reservations")
    public ResponseEntity<Reserva> createReservation(@RequestBody ReservaRequest request, Authentication auth) {
        Usuario actual = me(auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.crearReserva(request, actual));
    }
    @GetMapping("/admin/reservations")
    public List<Reserva> adminReservations(
            @RequestParam(value="date", required=false) LocalDate date,
            @RequestParam(value="courtId", required=false) Long courtId,
            @RequestParam(value="userId", required=false) Long userId,
            Authentication auth
    ) {
        Usuario me = me(auth);
        if (me.getRol() != Rol.ADMIN) throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        return reservaService.adminReservas(date, courtId, userId);
    }
    @GetMapping("/reservations/{reservationId}")
    public Reserva get(@PathVariable Long reservationId, Authentication auth) {
        Usuario me = me(auth);
        Reserva r = reservaService.get(reservationId);

        boolean admin = me.getRol() == Rol.ADMIN;
        boolean owner = r.getUsuario().getIdUsuario().equals(me.getIdUsuario());
        if (!admin && !owner) throw new org.springframework.security.access.AccessDeniedException("Forbidden");

        return r;
    }
    @GetMapping("/reservations")
    public List<Reserva> getMyReservations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication auth) {
        Usuario actual = me(auth);
        return reservaService.listarMisReservasFiltradas(actual, from, to);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id, Authentication auth) {
        Usuario actual = me(auth);
        reservaService.cancelarReserva(id, actual);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/reservations/{reservationId}")
    public Reserva patch(@PathVariable Long reservationId, @Valid @RequestBody ReservaUpdateRequest req, Authentication auth) {
        Usuario me = me(auth);
        return reservaService.reprogramar(reservationId, req, me);
    }



    private Usuario me(Authentication auth) {
        return usuarioRepo.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new NotFoundException("Usuario no existe"));
    }
}

package edu.comillas.icai.gitt.pat.spring.pista_padel_backend;

import edu.comillas.icai.gitt.pat.spring.*;
import edu.comillas.icai.gitt.pat.spring.dto.ReservaUpdateRequest;
import edu.comillas.icai.gitt.pat.spring.Excepciones.NotFoundException;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Reserva;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Usuario;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.UsuarioRepositorio;
import edu.comillas.icai.gitt.pat.spring.servicio.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/reservations/{reservationId}")
    public Reserva get(@PathVariable Long reservationId, Authentication auth) {
        Usuario me = me(auth);
        Reserva r = reservaService.get(reservationId);

        boolean admin = me.getRol() == Rol.ADMIN;
        boolean owner = r.getUsuario().getIdUsuario().equals(me.getIdUsuario());
        if (!admin && !owner) throw new org.springframework.security.access.AccessDeniedException("Forbidden");

        return r;
    }

    @PatchMapping("/reservations/{reservationId}")
    public Reserva patch(@PathVariable Long reservationId, @Valid @RequestBody ReservaUpdateRequest req, Authentication auth) {
        Usuario me = me(auth);
        return reservaService.reprogramar(reservationId, req, me);
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

    private Usuario me(Authentication auth) {
        return usuarioRepo.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new NotFoundException("Usuario no existe"));
    }
}

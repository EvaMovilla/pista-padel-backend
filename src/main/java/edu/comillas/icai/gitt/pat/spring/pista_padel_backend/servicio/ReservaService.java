package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.ReservaRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepositorio reservaRepositorio;
    private final PistaService pistaService;

    public ReservaService(ReservaRepositorio reservaRepositorio, PistaService pistaService) {
        this.reservaRepositorio = reservaRepositorio;
        this.pistaService = pistaService;
    }

    public Reserva crearReserva(ReservaRequest request, Usuario usuarioLogueado) {
        Pista pista = pistaService.obtenerPista(request.getCourtId());

        if (!pista.isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La pista no está activa para reservas");
        }

        LocalTime horaFin = request.getStartTime().plusMinutes(request.getDurationMinutes());

        boolean solapado = reservaRepositorio.existeSolapamiento(
                pista, request.getDate(), request.getStartTime(), horaFin
        );

        if (solapado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El slot horario ya está ocupado");
        }

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuarioLogueado);
        reserva.setPista(pista);
        reserva.setFechaReserva(request.getDate());
        reserva.setHoraInicio(request.getStartTime());
        reserva.setDuracionMinutos(request.getDurationMinutes());
        reserva.setHoraFin(horaFin);
        reserva.setEstado(EstadoReserva.ACTIVA);
        reserva.setFechaCreacion(LocalDateTime.now());

        return reservaRepositorio.save(reserva);
    }

    public Reserva obtenerReserva(Long idReserva, Usuario usuarioActual) {
        Reserva reserva = reservaRepositorio.findById(idReserva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        boolean esAdmin = usuarioActual.getRol() == Rol.ADMIN;
        boolean esDueno = reserva.getUsuario().getIdUsuario().equals(usuarioActual.getIdUsuario());

        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para ver esta reserva");
        }

        return reserva;
    }

    public List<Reserva> listarMisReservasFiltradas(Usuario usuarioActual, LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' no puede ser posterior a 'to'");
        }

        return reservaRepositorio.buscarConFiltros(
                usuarioActual.getIdUsuario(),
                null,
                null,
                from,
                to
        );
    }

    public List<Reserva> listarReservasAdmin(Long courtId, Long userId, EstadoReserva estado, LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' no puede ser posterior a 'to'");
        }

        return reservaRepositorio.buscarConFiltros(
                userId,
                courtId,
                estado,
                from,
                to
        );
    }

    public List<Reserva> listarMisReservas(Usuario usuarioLogueado) {
        return reservaRepositorio.findByUsuario_IdUsuario(usuarioLogueado.getIdUsuario());
    }

    public void cancelarReserva(Long reservationId, Usuario usuarioLogueado) {
        Reserva reserva = reservaRepositorio.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        if (!reserva.getUsuario().getIdUsuario().equals(usuarioLogueado.getIdUsuario())
                && usuarioLogueado.getRol() != Rol.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para cancelar esta reserva");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reservaRepositorio.save(reserva);
    }

    public List<Reserva> consultarDisponibilidad(Long idPista, LocalDate fecha) {
        return reservaRepositorio.findByPista_IdPistaAndFechaReservaAndEstado(
                idPista, fecha, EstadoReserva.ACTIVA
        );
    }
}

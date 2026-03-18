package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.Excepciones.ConflictException;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.Excepciones.NotFoundException;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.ReservaRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.ReservaUpdateRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservaService {

    private static final Logger log = LoggerFactory.getLogger(ReservaService.class);

    private final ReservaRepositorio reservaRepositorio;
    private final PistaService pistaService;

    public ReservaService(ReservaRepositorio reservaRepositorio, PistaService pistaService) {
        this.reservaRepositorio = reservaRepositorio;
        this.pistaService = pistaService;
    }

    // GET reserva por id (usado por ReservationsController)
    public Reserva get(Long reservaId) {
        return reservaRepositorio.findById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada"));
    }

    public Reserva crearReserva(ReservaRequest request, Usuario usuarioLogueado) {
        Pista pista = pistaService.obtenerPista(request.getCourtId());

        if (!pista.isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La pista no está activa para reservas");
        }

        LocalTime horaFin = request.getStartTime().plusMinutes(request.getDurationMinutes());

        if (reservaRepositorio.existeSolapamiento(pista, request.getDate(), request.getStartTime(), horaFin)) {
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

        Reserva saved = reservaRepositorio.save(reserva);
        log.info("Reserva {} creada", saved.getIdReserva());
        return saved;
    }

    public Reserva obtenerReserva(Long idReserva, Usuario usuarioActual) {
        Reserva reserva = get(idReserva);

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
        return reservaRepositorio.buscarConFiltros(usuarioActual.getIdUsuario(), null, null, from, to);
    }

    public List<Reserva> listarReservasAdmin(Long courtId, Long userId, EstadoReserva estado, LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' no puede ser posterior a 'to'");
        }
        return reservaRepositorio.buscarConFiltros(userId, courtId, estado, from, to);
    }

    // Alias usado por ReservationsController
    public List<Reserva> adminReservas(LocalDate fecha, Long courtId, Long userId) {
        return reservaRepositorio.adminFilter(fecha, courtId, userId);
    }

    public List<Reserva> listarMisReservas(Usuario usuarioLogueado) {
        return reservaRepositorio.findByUsuario_IdUsuario(usuarioLogueado.getIdUsuario());
    }

    public void cancelarReserva(Long reservationId, Usuario usuarioLogueado) {
        Reserva reserva = get(reservationId);

        if (!reserva.getUsuario().getIdUsuario().equals(usuarioLogueado.getIdUsuario())
                && usuarioLogueado.getRol() != Rol.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para cancelar esta reserva");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reservaRepositorio.save(reserva);
        log.info("Reserva {} cancelada", reservationId);
    }

    @Transactional
    public Reserva reprogramar(Long reservaId, ReservaUpdateRequest req, Usuario requester) {
        Reserva r = get(reservaId);
        checkOwnerOrAdmin(r, requester);

        if (r.getEstado() != EstadoReserva.ACTIVA) {
            throw new ConflictException("No se puede modificar una reserva cancelada");
        }

        Pista pista = r.getPista();
        if (!pista.isActiva()) throw new ConflictException("Pista inactiva");

        LocalTime fin = req.horaInicio().plusMinutes(req.duracionMinutos());

        boolean cambia = !req.fechaReserva().equals(r.getFechaReserva())
                || !req.horaInicio().equals(r.getHoraInicio())
                || req.duracionMinutos() != r.getDuracionMinutos();

        if (cambia) {
            var overlaps = reservaRepositorio.findOverlaps(pista.getIdPista(), req.fechaReserva(), req.horaInicio(), fin)
                    .stream()
                    .filter(x -> !x.getIdReserva().equals(r.getIdReserva()))
                    .toList();
            if (!overlaps.isEmpty()) throw new ConflictException("Nuevo slot ocupado");
        }

        r.setFechaReserva(req.fechaReserva());
        r.setHoraInicio(req.horaInicio());
        r.setDuracionMinutos(req.duracionMinutos());
        r.setHoraFin(fin);

        log.info("Reserva {} reprogramada", reservaId);
        return r;
    }

    public List<Reserva> consultarDisponibilidad(Long idPista, LocalDate fecha) {
        return reservaRepositorio.findByPista_IdPistaAndFechaReservaAndEstado(idPista, fecha, EstadoReserva.ACTIVA);
    }

    private void checkOwnerOrAdmin(Reserva r, Usuario requester) {
        boolean admin = requester.getRol() == Rol.ADMIN;
        boolean owner = r.getUsuario().getIdUsuario().equals(requester.getIdUsuario());
        if (!admin && !owner) throw new org.springframework.security.access.AccessDeniedException("Forbidden");
    }
}

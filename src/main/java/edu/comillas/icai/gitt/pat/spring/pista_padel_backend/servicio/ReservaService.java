package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.Excepciones.ConflictException;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.ReservaRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.ReservaUpdateRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        this.pistaService = pistaService; // Reutilizamos el servicio de pistas
    }

    public Reserva crearReserva(ReservaRequest request, Usuario usuarioLogueado) {
        // 1. Obtener la pista (lanzará 404 si no existe)
        Pista pista = pistaService.obtenerPista(request.getCourtId());

        // 2. Regla: No reservar pista inactiva
        if (!pista.isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La pista no está activa para reservas");
        }

        // 3. Calcular la hora de fin
        LocalTime horaFin = request.getStartTime().plusMinutes(request.getDurationMinutes());

        // 4. Regla: No solapamiento  (Usamos la query mágica que creamos en el Repositorio)
        boolean solapado = reservaRepositorio.existeSolapamiento(
                pista, request.getDate(), request.getStartTime(), horaFin
        );
        if (solapado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El slot horario ya está ocupado"); // 409
        }

        // 5. Crear y guardar la reserva
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

    public List<Reserva> listarMisReservas(Usuario usuarioLogueado) {
        return reservaRepositorio.findByUsuario_IdUsuario(usuarioLogueado.getIdUsuario());
    }

    public void cancelarReserva(Long reservationId, Usuario usuarioLogueado) {
        Reserva reserva = reservaRepositorio.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        // Regla: Solo dueño (o ADMIN, aunque aquí validamos dueño) puede modificar/cancelar
        if (!reserva.getUsuario().getIdUsuario().equals(usuarioLogueado.getIdUsuario())
                && usuarioLogueado.getRol() != Rol.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para cancelar esta reserva");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reservaRepositorio.save(reserva);
    }

    public List<Reserva> consultarDisponibilidad(Long idPista, LocalDate fecha) {
        // Retornamos las reservas activas para que el frontal sepa qué horas están pilladas
        return reservaRepositorio.findByPista_IdPistaAndFechaReservaAndEstado(
                idPista, fecha, EstadoReserva.ACTIVA
        );
    }

    @Transactional
    public Reserva crearReserva(Long idUsuario, Long idPista, LocalDate fecha, LocalTime inicio, int duracionMin) {
        Usuario usuario = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuario no existe"));

        Pista pista = pistaRepo.findById(idPista)
                .orElseThrow(() -> new NotFoundException("Pista no existe"));

        if (!pista.isActiva()) {
            throw new ConflictException("No se puede reservar una pista inactiva");
        }

        LocalTime fin = inicio.plusMinutes(duracionMin);

        if (!reservaRepo.findOverlaps(idPista, fecha, inicio, fin).isEmpty()) {
            throw new ConflictException("Slot ocupado");
        }

        Reserva r = new Reserva();
        r.setUsuario(usuario);
        r.setPista(pista);
        r.setFechaReserva(fecha);
        r.setHoraInicio(inicio);
        r.setDuracionMinutos(duracionMin);
        r.setHoraFin(fin);
        r.setEstado(EstadoReserva.ACTIVA);
        r.setFechaCreacion(LocalDateTime.now());

        Reserva saved = reservaRepo.save(r);
        log.info("Reserva {} creada por usuario {}", saved.getIdReserva(), idUsuario);
        return saved;
    }

    @Transactional
    public void cancelar(Long reservaId, Usuario requester) {
        Reserva r = get(reservaId);
        checkOwnerOrAdmin(r, requester);

        if (r.getEstado() == EstadoReserva.CANCELADA) return;

        r.setEstado(EstadoReserva.CANCELADA);
        log.info("Reserva {} cancelada", reservaId);
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

        // si cambia algo, comprobar solapamientos
        boolean cambia = !req.fechaReserva().equals(r.getFechaReserva())
                || !req.horaInicio().equals(r.getHoraInicio())
                || req.duracionMinutos() != r.getDuracionMinutos();

        if (cambia) {
            var overlaps = reservaRepo.findOverlaps(pista.getIdPista(), req.fechaReserva(), req.horaInicio(), fin)
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

    private void checkOwnerOrAdmin(Reserva r, Usuario requester) {
        boolean admin = requester.getRol() == Rol.ADMIN;
        boolean owner = r.getUsuario().getIdUsuario().equals(requester.getIdUsuario());
        if (!admin && !owner) throw new org.springframework.security.access.AccessDeniedException("Forbidden");
    }

}

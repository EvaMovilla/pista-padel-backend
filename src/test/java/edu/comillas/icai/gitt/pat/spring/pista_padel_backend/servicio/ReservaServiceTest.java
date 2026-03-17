package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.ReservaRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.EstadoReserva;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Reserva;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.ReservaRepositorio;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepositorio reservaRepositorio;

    @Mock
    private PistaService pistaService;

    @InjectMocks
    private ReservaService reservaService;

    @Test
    void crearReserva_lanza409_siHaySolapamiento() {
        // given
        Usuario usuario = crearUsuario(1L, Rol.USUARIO);
        Pista pista = crearPista(10L, true);

        ReservaRequest req = new ReservaRequest();
        req.setCourtId(10L);
        req.setDate(LocalDate.of(2025, 3, 10));
        req.setStartTime(LocalTime.of(18, 0));
        req.setDurationMinutes(90);

        when(pistaService.obtenerPista(10L)).thenReturn(pista);
        when(reservaRepositorio.existeSolapamiento(
                eq(pista),
                eq(LocalDate.of(2025, 3, 10)),
                eq(LocalTime.of(18, 0)),
                eq(LocalTime.of(19, 30))
        )).thenReturn(true);

        // when + then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservaService.crearReserva(req, usuario));

        assertEquals(409, ex.getStatusCode().value());
        verify(reservaRepositorio, never()).save(any());
    }

    @Test
    void cancelarReserva_permiteCancelar_siEsElDueno() {
        // given
        Usuario usuario = crearUsuario(1L, Rol.USUARIO);
        Pista pista = crearPista(10L, true);
        Reserva reserva = crearReserva(100L, usuario, pista);

        when(reservaRepositorio.findById(100L)).thenReturn(Optional.of(reserva));

        // when
        reservaService.cancelarReserva(100L, usuario);

        // then
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        verify(reservaRepositorio).save(reserva);
    }

    @Test
    void cancelarReserva_permiteCancelar_siEsAdmin() {
        // given
        Usuario dueno = crearUsuario(1L, Rol.USUARIO);
        Usuario admin = crearUsuario(99L, Rol.ADMIN);
        Pista pista = crearPista(10L, true);
        Reserva reserva = crearReserva(100L, dueno, pista);

        when(reservaRepositorio.findById(100L)).thenReturn(Optional.of(reserva));

        // when
        reservaService.cancelarReserva(100L, admin);

        // then
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        verify(reservaRepositorio).save(reserva);
    }

    @Test
    void cancelarReserva_lanza403_siNoTienePermisos() {
        // given
        Usuario dueno = crearUsuario(1L, Rol.USUARIO);
        Usuario otro = crearUsuario(2L, Rol.USUARIO);
        Pista pista = crearPista(10L, true);
        Reserva reserva = crearReserva(100L, dueno, pista);

        when(reservaRepositorio.findById(100L)).thenReturn(Optional.of(reserva));

        // when + then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservaService.cancelarReserva(100L, otro));

        assertEquals(403, ex.getStatusCode().value());
        verify(reservaRepositorio, never()).save(any());
    }

    @Test
    void obtenerReserva_devuelveReserva_siEsElDueno() {
        // given
        Usuario usuario = crearUsuario(1L, Rol.USUARIO);
        Pista pista = crearPista(10L, true);
        Reserva reserva = crearReserva(100L, usuario, pista);

        when(reservaRepositorio.findById(100L)).thenReturn(Optional.of(reserva));

        // when
        Reserva res = reservaService.obtenerReserva(100L, usuario);

        // then
        assertSame(reserva, res);
    }

    @Test
    void obtenerReserva_devuelveReserva_siEsAdmin() {
        // given
        Usuario dueno = crearUsuario(1L, Rol.USUARIO);
        Usuario admin = crearUsuario(99L, Rol.ADMIN);
        Pista pista = crearPista(10L, true);
        Reserva reserva = crearReserva(100L, dueno, pista);

        when(reservaRepositorio.findById(100L)).thenReturn(Optional.of(reserva));

        // when
        Reserva res = reservaService.obtenerReserva(100L, admin);

        // then
        assertSame(reserva, res);
    }

    @Test
    void obtenerReserva_lanza403_siNoTienePermisos() {
        // given
        Usuario dueno = crearUsuario(1L, Rol.USUARIO);
        Usuario otro = crearUsuario(2L, Rol.USUARIO);
        Pista pista = crearPista(10L, true);
        Reserva reserva = crearReserva(100L, dueno, pista);

        when(reservaRepositorio.findById(100L)).thenReturn(Optional.of(reserva));

        // when + then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservaService.obtenerReserva(100L, otro));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void listarMisReservasFiltradas_devuelveReservasDelUsuario() {
        // given
        Usuario usuario = crearUsuario(1L, Rol.USUARIO);
        Reserva reserva = crearReserva(100L, usuario, crearPista(10L, true));

        LocalDate from = LocalDate.of(2025, 3, 1);
        LocalDate to = LocalDate.of(2025, 3, 31);

        when(reservaRepositorio.buscarConFiltros(1L, null, null, from, to))
                .thenReturn(List.of(reserva));

        // when
        List<Reserva> res = reservaService.listarMisReservasFiltradas(usuario, from, to);

        // then
        assertEquals(1, res.size());
        verify(reservaRepositorio).buscarConFiltros(1L, null, null, from, to);
    }

    @Test
    void listarMisReservasFiltradas_lanza400_siFromEsPosteriorATo() {
        // given
        Usuario usuario = crearUsuario(1L, Rol.USUARIO);
        LocalDate from = LocalDate.of(2025, 4, 1);
        LocalDate to = LocalDate.of(2025, 3, 1);

        // when + then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservaService.listarMisReservasFiltradas(usuario, from, to));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void listarReservasAdmin_devuelveReservasFiltradas() {
        // given
        Usuario usuario = crearUsuario(1L, Rol.USUARIO);
        Reserva reserva = crearReserva(100L, usuario, crearPista(10L, true));

        Long courtId = 10L;
        Long userId = 1L;
        EstadoReserva estado = EstadoReserva.ACTIVA;
        LocalDate from = LocalDate.of(2025, 3, 1);
        LocalDate to = LocalDate.of(2025, 3, 31);

        when(reservaRepositorio.buscarConFiltros(userId, courtId, estado, from, to))
                .thenReturn(List.of(reserva));

        // when
        List<Reserva> res = reservaService.listarReservasAdmin(courtId, userId, estado, from, to);

        // then
        assertEquals(1, res.size());
        verify(reservaRepositorio).buscarConFiltros(userId, courtId, estado, from, to);
    }

    @Test
    void listarReservasAdmin_lanza400_siFromEsPosteriorATo() {
        // given
        LocalDate from = LocalDate.of(2025, 4, 1);
        LocalDate to = LocalDate.of(2025, 3, 1);

        // when + then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reservaService.listarReservasAdmin(10L, 1L, EstadoReserva.ACTIVA, from, to));

        assertEquals(400, ex.getStatusCode().value());
    }

    private Usuario crearUsuario(Long id, Rol rol) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setRol(rol);
        u.setActivo(true);
        u.setNombre("Usuario");
        u.setApellidos("Test");
        u.setEmail("test" + id + "@mail.com");
        u.setPassword("1234");
        u.setFechaRegistro(LocalDateTime.now());
        return u;
    }

    private Pista crearPista(Long id, boolean activa) {
        Pista p = new Pista();
        p.setIdPista(id);
        p.setNombre("Pista " + id);
        p.setUbicacion("Madrid");
        p.setPrecioHora(20.0);
        p.setActiva(activa);
        p.setFechaAlta(LocalDateTime.now());
        return p;
    }

    private Reserva crearReserva(Long id, Usuario usuario, Pista pista) {
        Reserva r = new Reserva();
        r.setIdReserva(id);
        r.setUsuario(usuario);
        r.setPista(pista);
        r.setFechaReserva(LocalDate.of(2025, 3, 10));
        r.setHoraInicio(LocalTime.of(18, 0));
        r.setDuracionMinutos(90);
        r.setHoraFin(LocalTime.of(19, 30));
        r.setEstado(EstadoReserva.ACTIVA);
        r.setFechaCreacion(LocalDateTime.now());
        return r;
    }
}
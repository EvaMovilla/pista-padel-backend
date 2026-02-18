package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.PistaRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.PistaRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PistaServiceTest {

    @Mock
    private PistaRepositorio pistaRepositorio;

    @InjectMocks
    private PistaService pistaService;

    @Test
    void crearPista_lanza409_siNombreYaExiste() {
        // given
        PistaRequest req = new PistaRequest();
        req.setNombre("Central");
        req.setUbicacion("Madrid");
        req.setPrecioHora(20.0);
        req.setActiva(true);

        when(pistaRepositorio.existsByNombre("Central")).thenReturn(true);

        // when + then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pistaService.crearPista(req));

        assertEquals(409, ex.getStatusCode().value());
        verify(pistaRepositorio, never()).save(any());
    }

    @Test
    void crearPista_guardaYDevuelvePista_siNombreNoExiste() {
        // given
        PistaRequest req = new PistaRequest();
        req.setNombre("Central");
        req.setUbicacion("Madrid");
        req.setPrecioHora(20.0);
        req.setActiva(true);

        when(pistaRepositorio.existsByNombre("Central")).thenReturn(false);
        when(pistaRepositorio.save(any(Pista.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Pista res = pistaService.crearPista(req);

        // then
        assertNotNull(res);
        assertEquals("Central", res.getNombre());
        assertEquals("Madrid", res.getUbicacion());
        assertEquals(20.0, res.getPrecioHora());
        assertNotNull(res.getFechaAlta());

        // (extra) verificamos qué se guardó
        ArgumentCaptor<Pista> captor = ArgumentCaptor.forClass(Pista.class);
        verify(pistaRepositorio).save(captor.capture());
        assertEquals("Central", captor.getValue().getNombre());
    }

    @Test
    void listarPistas_siActivaEsNull_usaFindAll() {
        // given
        when(pistaRepositorio.findAll()).thenReturn(List.of());

        // when
        List<Pista> res = pistaService.listarPistas(null);

        // then
        assertNotNull(res);
        verify(pistaRepositorio).findAll();
        verify(pistaRepositorio, never()).findByActiva(anyBoolean());
    }

    @Test
    void listarPistas_siActivaNoEsNull_usaFindByActiva() {
        // given
        when(pistaRepositorio.findByActiva(true)).thenReturn(List.of());

        // when
        List<Pista> res = pistaService.listarPistas(true);

        // then
        assertNotNull(res);
        verify(pistaRepositorio).findByActiva(true);
        verify(pistaRepositorio, never()).findAll();
    }

    @Test
    void obtenerPista_lanza404_siNoExiste() {
        // given
        when(pistaRepositorio.findById(1L)).thenReturn(Optional.empty());

        // when + then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pistaService.obtenerPista(1L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void obtenerPista_devuelvePista_siExiste() {
        // given
        Pista p = new Pista();
        when(pistaRepositorio.findById(1L)).thenReturn(Optional.of(p));

        // when
        Pista res = pistaService.obtenerPista(1L);

        // then
        assertSame(p, res);
    }
}
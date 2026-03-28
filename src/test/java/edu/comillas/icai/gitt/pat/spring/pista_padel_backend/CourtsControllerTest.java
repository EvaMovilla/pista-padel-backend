package edu.comillas.icai.gitt.pat.spring.pista_padel_backend;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CourtsControllerTest {

    @Autowired MockMvc mvc;
    @Autowired PistaRepositorio pistaRepo;
    @Autowired ReservaRepositorio reservaRepo;
    @Autowired UsuarioRepositorio usuarioRepo;
    @Autowired PasswordEncoder passwordEncoder;

    private Long pistaId;
    private Usuario admin;
    private Usuario cliente;

    @BeforeEach
    void setUp() {
        reservaRepo.deleteAll();
        pistaRepo.deleteAll();
        usuarioRepo.deleteAll();

        admin = new Usuario();
        admin.setNombre("Admin");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("pass"));
        admin.setRol(Rol.ADMIN);
        admin = usuarioRepo.save(admin);

        cliente = new Usuario();
        cliente.setNombre("Cliente");
        cliente.setEmail("cliente@test.com");
        cliente.setPassword(passwordEncoder.encode("pass"));
        cliente.setRol(Rol.USER);
        cliente = usuarioRepo.save(cliente);

        Pista p = new Pista();
        p.setNombre("Pista Test");
        p.setUbicacion("Zona A");
        p.setPrecioHora(15.0);
        p.setActiva(true);
        p.setFechaAlta(LocalDateTime.now());
        pistaId = pistaRepo.save(p).getIdPista();
    }

    // =========================================================
    // GET /courts/{courtId}
    // =========================================================

    @Test
    @WithMockUser(username = "admin@test.com")
    void getCourt_existente_retorna200YDatos() throws Exception {
        mvc.perform(get("/pistaPadel/courts/" + pistaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pista Test"))
                .andExpect(jsonPath("$.ubicacion").value("Zona A"))
                .andExpect(jsonPath("$.activa").value(true));
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void getCourt_noExiste_retorna404() throws Exception {
        mvc.perform(get("/pistaPadel/courts/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCourt_sinAutenticar_retorna401() throws Exception {
        mvc.perform(get("/pistaPadel/courts/" + pistaId))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // PATCH /courts/{courtId}
    // =========================================================

    @Test
    @WithMockUser(username = "admin@test.com")
    void patchCourt_admin_actualizaUbicacionYPrecio() throws Exception {
        mvc.perform(patch("/pistaPadel/courts/" + pistaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "ubicacion": "Zona B", "precioHora": 25.0 }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ubicacion").value("Zona B"))
                .andExpect(jsonPath("$.precioHora").value(25.0));
    }

    @Test
    @WithMockUser(username = "cliente@test.com")
    void patchCourt_cliente_retorna403() throws Exception {
        mvc.perform(patch("/pistaPadel/courts/" + pistaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "ubicacion": "Zona C" }
                    """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void patchCourt_nombreDuplicado_retorna409() throws Exception {
        Pista otra = new Pista();
        otra.setNombre("Pista Ocupada");
        otra.setActiva(true);
        otra.setFechaAlta(LocalDateTime.now());
        pistaRepo.save(otra);

        mvc.perform(patch("/pistaPadel/courts/" + pistaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "nombre": "Pista Ocupada" }
                    """))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void patchCourt_pistaNoExiste_retorna404() throws Exception {
        mvc.perform(patch("/pistaPadel/courts/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "ubicacion": "Zona X" }
                    """))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // DELETE /courts/{courtId}
    // =========================================================

    @Test
    @WithMockUser(username = "admin@test.com")
    void deleteCourt_sinReservasFuturas_borradoFisicoRetorna204() throws Exception {
        mvc.perform(delete("/pistaPadel/courts/" + pistaId))
                .andExpect(status().isNoContent());

        assertThat(pistaRepo.findById(pistaId)).isEmpty();
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void deleteCourt_conReservasFuturas_desactivacionLogicaRetorna204() throws Exception {
        Pista pista = pistaRepo.findById(pistaId).orElseThrow();

        Reserva reserva = new Reserva();
        reserva.setPista(pista);
        reserva.setUsuario(cliente);
        reserva.setFechaReserva(LocalDate.now().plusDays(3));
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setHoraFin(LocalTime.of(11, 0));
        reserva.setDuracionMinutos(60);
        reserva.setEstado(EstadoReserva.ACTIVA);
        reserva.setFechaCreacion(LocalDateTime.now());
        reservaRepo.save(reserva);

        mvc.perform(delete("/pistaPadel/courts/" + pistaId))
                .andExpect(status().isNoContent());

        // Pista sigue en BD pero inactiva
        Pista resultado = pistaRepo.findById(pistaId).orElseThrow();
        assertThat(resultado.isActiva()).isFalse();
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void deleteCourt_conReservasPasadas_borradoFisico() throws Exception {
        Pista pista = pistaRepo.findById(pistaId).orElseThrow();

        // Reserva en el pasado → no bloquea borrado físico
        Reserva reserva = new Reserva();
        reserva.setPista(pista);
        reserva.setUsuario(cliente);
        reserva.setFechaReserva(LocalDate.now().minusDays(5));
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setHoraFin(LocalTime.of(11, 0));
        reserva.setDuracionMinutos(60);
        reserva.setEstado(EstadoReserva.ACTIVA);
        reserva.setFechaCreacion(LocalDateTime.now());
        reservaRepo.save(reserva);

        mvc.perform(delete("/pistaPadel/courts/" + pistaId))
                .andExpect(status().isNoContent());

        assertThat(pistaRepo.findById(pistaId)).isEmpty();
    }

    @Test
    @WithMockUser(username = "cliente@test.com")
    void deleteCourt_cliente_retorna403() throws Exception {
        mvc.perform(delete("/pistaPadel/courts/" + pistaId))
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // GET /courts/{courtId}/availability
    // =========================================================

    @Test
    @WithMockUser(username = "admin@test.com")
    void getAvailability_sinReservas_retornaListaVacia() throws Exception {
        mvc.perform(get("/pistaPadel/courts/" + pistaId + "/availability")
                        .param("date", "2026-04-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-04-01"))
                .andExpect(jsonPath("$.courts[0].courtId").value(pistaId))
                .andExpect(jsonPath("$.courts[0].reservas").isEmpty());
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void getAvailability_conReserva_retornaSlotOcupado() throws Exception {
        Pista pista = pistaRepo.findById(pistaId).orElseThrow();

        Reserva reserva = new Reserva();
        reserva.setPista(pista);
        reserva.setUsuario(cliente);
        reserva.setFechaReserva(LocalDate.of(2026, 4, 1));
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setHoraFin(LocalTime.of(11, 0));
        reserva.setDuracionMinutos(60);
        reserva.setEstado(EstadoReserva.ACTIVA);
        reserva.setFechaCreacion(LocalDateTime.now());
        reservaRepo.save(reserva);

        mvc.perform(get("/pistaPadel/courts/" + pistaId + "/availability")
                        .param("date", "2026-04-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courts[0].reservas[0].inicio").value("10:00:00"))
                .andExpect(jsonPath("$.courts[0].reservas[0].fin").value("11:00:00"));
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void getAvailability_pistaNoExiste_retorna404() throws Exception {
        mvc.perform(get("/pistaPadel/courts/9999/availability")
                        .param("date", "2026-04-01"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void getAvailability_sinFecha_retorna400() throws Exception {
        mvc.perform(get("/pistaPadel/courts/" + pistaId + "/availability"))
                .andExpect(status().isBadRequest());
    }
}

package edu.comillas.icai.gitt.pat.spring.pista_padel_backend;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Usuario;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepositorio usuarioRepo;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepo.deleteAll();

        Usuario u = new Usuario();
        u.setNombre("Ana");
        u.setApellidos("García");
        u.setEmail("ana@test.com");
        u.setTelefono("666111222");
        u.setPassword(passwordEncoder.encode("pass123"));
        u.setRol(Rol.USER);
        u.setActivo(true);
        u.setFechaRegistro(LocalDateTime.now());
        usuarioRepo.save(u);
    }

    @Test
    void register_ok_retorna201YGuardaUsuario() throws Exception {
        mvc.perform(post("/pistaPadel/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nombre": "Luis",
                              "apellidos": "Pérez",
                              "email": "luis@test.com",
                              "password": "secreta123",
                              "telefono": "600123123"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("idUsuario")));

        Usuario guardado = usuarioRepo.findByEmail("luis@test.com").orElse(null);
        assertThat(guardado).isNotNull();
        assertThat(guardado.getRol()).isEqualTo(Rol.USER);
        assertThat(guardado.isActivo()).isTrue();
        assertThat(passwordEncoder.matches("secreta123", guardado.getPassword())).isTrue();
    }

    @Test
    void register_emailDuplicado_retorna409() throws Exception {
        mvc.perform(post("/pistaPadel/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nombre": "Otro",
                              "apellidos": "Usuario",
                              "email": "ana@test.com",
                              "password": "xxxxxx",
                              "telefono": "600000000"
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("EMAIL_EXISTS")));
    }

    @Test
    void register_datosInvalidos_retorna400() throws Exception {
        mvc.perform(post("/pistaPadel/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nombre": "",
                              "apellidos": "",
                              "email": "correo-mal",
                              "password": ""
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ok_retorna200YPermiteAccederAMeConLaSesion() throws Exception {
        MvcResult result = mvc.perform(post("/pistaPadel/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "ana@test.com",
                              "password": "pass123"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Login correcto")))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mvc.perform(get("/pistaPadel/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@test.com"))
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.rol").value("USER"));
    }

    @Test
    void login_credencialesIncorrectas_retorna401() throws Exception {
        mvc.perform(post("/pistaPadel/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "ana@test.com",
                              "password": "mal"
                            }
                            """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("BAD_CREDENTIALS")));
    }

    @Test
    void me_sinAutenticar_retorna401() throws Exception {
        mvc.perform(get("/pistaPadel/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_conSesion_retorna200() throws Exception {
        MvcResult result = mvc.perform(post("/pistaPadel/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "ana@test.com",
                              "password": "pass123"
                            }
                            """))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mvc.perform(post("/pistaPadel/auth/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Logout correcto")));
    }
}
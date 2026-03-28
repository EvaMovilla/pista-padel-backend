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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UsersControllerTest {

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepositorio usuarioRepo;
    @Autowired PasswordEncoder passwordEncoder;

    private Usuario admin;
    private Usuario user1;
    private Usuario user2;

    @BeforeEach
    void setUp() {
        usuarioRepo.deleteAll();

        admin = new Usuario();
        admin.setNombre("Admin");
        admin.setApellidos("Root");
        admin.setEmail("admin@test.com");
        admin.setTelefono("600000001");
        admin.setPassword(passwordEncoder.encode("pass"));
        admin.setRol(Rol.ADMIN);
        admin.setActivo(true);
        admin.setFechaRegistro(LocalDateTime.now());
        admin = usuarioRepo.save(admin);

        user1 = new Usuario();
        user1.setNombre("Mario");
        user1.setApellidos("Uno");
        user1.setEmail("user1@test.com");
        user1.setTelefono("600000002");
        user1.setPassword(passwordEncoder.encode("pass"));
        user1.setRol(Rol.USER);
        user1.setActivo(true);
        user1.setFechaRegistro(LocalDateTime.now());
        user1 = usuarioRepo.save(user1);

        user2 = new Usuario();
        user2.setNombre("Lucia");
        user2.setApellidos("Dos");
        user2.setEmail("user2@test.com");
        user2.setTelefono("600000003");
        user2.setPassword(passwordEncoder.encode("pass"));
        user2.setRol(Rol.USER);
        user2.setActivo(true);
        user2.setFechaRegistro(LocalDateTime.now());
        user2 = usuarioRepo.save(user2);
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void list_admin_retorna200() throws Exception {
        mvc.perform(get("/pistaPadel/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @WithMockUser(username = "user1@test.com", roles = "USER")
    void list_user_retorna403() throws Exception {
        mvc.perform(get("/pistaPadel/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_sinAutenticar_retorna401() throws Exception {
        mvc.perform(get("/pistaPadel/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void get_admin_puedeVerCualquiera() throws Exception {
        mvc.perform(get("/pistaPadel/users/" + user1.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user1@test.com"));
    }

    @Test
    @WithMockUser(username = "user1@test.com", roles = "USER")
    void get_usuario_puedeVerseASiMismo() throws Exception {
        mvc.perform(get("/pistaPadel/users/" + user1.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user1@test.com"));
    }

    @Test
    @WithMockUser(username = "user1@test.com", roles = "USER")
    void get_usuario_noPuedeVerAOtro_retorna403() throws Exception {
        mvc.perform(get("/pistaPadel/users/" + user2.getIdUsuario()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user1@test.com", roles = "USER")
    void patch_usuario_puedeEditarSuPropioPerfil() throws Exception {
        mvc.perform(patch("/pistaPadel/users/" + user1.getIdUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nombre": "MarioActualizado",
                              "telefono": "699999999"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("MarioActualizado"))
                .andExpect(jsonPath("$.telefono").value("699999999"));

        Usuario actualizado = usuarioRepo.findById(user1.getIdUsuario()).orElseThrow();
        assertThat(actualizado.getNombre()).isEqualTo("MarioActualizado");
        assertThat(actualizado.getTelefono()).isEqualTo("699999999");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void patch_admin_puedeEditarAOtroUsuario() throws Exception {
        mvc.perform(patch("/pistaPadel/users/" + user1.getIdUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "activo": false,
                              "apellidos": "Cambiado"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false))
                .andExpect(jsonPath("$.apellidos").value("Cambiado"));

        Usuario actualizado = usuarioRepo.findById(user1.getIdUsuario()).orElseThrow();
        assertThat(actualizado.isActivo()).isFalse();
        assertThat(actualizado.getApellidos()).isEqualTo("Cambiado");
    }

    @Test
    @WithMockUser(username = "user1@test.com", roles = "USER")
    void patch_usuario_noPuedeEditarAOtro_retorna403() throws Exception {
        mvc.perform(patch("/pistaPadel/users/" + user2.getIdUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nombre": "Hack"
                            }
                            """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void patch_emailDuplicado_retorna409() throws Exception {
        mvc.perform(patch("/pistaPadel/users/" + user1.getIdUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "user2@test.com"
                            }
                            """))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void get_userNoExiste_retorna404() throws Exception {
        mvc.perform(get("/pistaPadel/users/999999"))
                .andExpect(status().isNotFound());
    }
}
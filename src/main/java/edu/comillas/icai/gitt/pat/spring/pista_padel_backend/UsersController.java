package edu.comillas.icai.gitt.pat.spring.pista_padel_backend;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.UserUpdateRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Usuario;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.UsuarioRepositorio;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pistaPadel/users")
public class UsersController {

    private final UsuarioService usuarioService;
    private final UsuarioRepositorio usuarioRepo;

    public UsersController(UsuarioService usuarioService, UsuarioRepositorio usuarioRepo) {
        this.usuarioService = usuarioService;
        this.usuarioRepo = usuarioRepo;
    }

    @GetMapping
    public List<Usuario> list(Authentication auth) {
        Usuario me = me(auth);
        if (me.getRol() != Rol.ADMIN) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }
        return usuarioService.listAll();
    }

    @GetMapping("/{userId}")
    public Usuario get(@PathVariable Long userId, Authentication auth) {
        Usuario me = me(auth);
        if (me.getRol() != Rol.ADMIN && !me.getIdUsuario().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }
        return usuarioService.getById(userId);
    }

    @PatchMapping("/{userId}")
    public Usuario patch(@PathVariable Long userId,
                         @Valid @RequestBody UserUpdateRequest req,
                         Authentication auth) {
        Usuario me = me(auth);
        if (me.getRol() != Rol.ADMIN && !me.getIdUsuario().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }
        return usuarioService.patchUser(userId, req);
    }

    private Usuario me(Authentication auth) {
        return usuarioRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no existe"));

    }
}

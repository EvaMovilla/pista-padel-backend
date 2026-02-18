package edu.comillas.icai.gitt.pat.spring.pista_padel_backend;

import edu.comillas.icai.gitt.pat.spring.*;
import edu.comillas.icai.gitt.pat.spring.dto.PistaUpdateRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Usuario;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.UsuarioRepositorio;
import edu.comillas.icai.gitt.pat.spring.servicio.PistaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pistaPadel/courts")
public class CourtsController {

    private final PistaService pistaService;
    private final UsuarioRepositorio usuarioRepo;

    public CourtsController(PistaService pistaService, UsuarioRepositorio usuarioRepo) {
        this.pistaService = pistaService;
        this.usuarioRepo = usuarioRepo;
    }

    @GetMapping
    public List<Pista> list() {
        return pistaService.list();
    }

    @GetMapping("/{courtId}")
    public Pista get(@PathVariable Long courtId) {
        return pistaService.get(courtId);
    }

    @PatchMapping("/{courtId}")
    public Pista patch(@PathVariable Long courtId, @Valid @RequestBody PistaUpdateRequest req, Authentication auth) {
        requireAdmin(auth);
        return pistaService.patch(courtId, req);
    }

    @DeleteMapping("/{courtId}")
    public ResponseEntity<Void> delete(@PathVariable Long courtId, Authentication auth) {
        requireAdmin(auth);
        pistaService.delete(courtId);
        return ResponseEntity.noContent().build(); // 204
    }

    private void requireAdmin(Authentication auth) {
        Usuario me = usuarioRepo.findByEmailIgnoreCase(auth.getName()).orElseThrow();
        if (me.getRol() != Rol.ADMIN) throw new org.springframework.security.access.AccessDeniedException("Forbidden");
    }
}
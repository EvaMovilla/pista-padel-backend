package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio;

import edu.comillas.icai.gitt.pat.spring.*;
import edu.comillas.icai.gitt.pat.spring.dto.UserUpdateRequest;
import edu.comillas.icai.gitt.pat.spring.Excepciones.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {
    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepositorio usuarioRepo;

    public UsuarioService(UsuarioRepositorio usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    public List<Usuario> listAll() {
        return usuarioRepo.findAll();
    }

    public Usuario getById(Long id) {
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no existe"));
    }

    @Transactional
    public Usuario patchUser(Long id, UserUpdateRequest req) {
        Usuario u = getById(id);

        if (req.nombre() != null) u.setNombre(req.nombre());
        if (req.apellidos() != null) u.setApellidos(req.apellidos());
        if (req.telefono() != null) u.setTelefono(req.telefono());
        if (req.activo() != null) u.setActivo(req.activo());

        if (req.email() != null && !req.email().equalsIgnoreCase(u.getEmail())) {
            if (usuarioRepo.existsByEmailIgnoreCase(req.email())) {
                throw new ConflictException("Email ya existe");
            }
            u.setEmail(req.email());
        }

        log.info("Usuario {} actualizado", id);
        return u;
    }
}

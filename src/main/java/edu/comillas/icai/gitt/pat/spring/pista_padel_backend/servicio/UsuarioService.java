package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.UserUpdateRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Usuario;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.UsuarioRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepositorio usuarioRepo;

    public UsuarioService(UsuarioRepositorio usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    public List<Usuario> listAll() {
        return usuarioRepo.findAll();
    }

    public Usuario getById(Long id) {
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @Transactional
    public Usuario patchUser(Long id, UserUpdateRequest req) {
        Usuario u = getById(id);

        if (req.getNombre() != null) {
            u.setNombre(req.getNombre());
        }

        if (req.getApellidos() != null) {
            u.setApellidos(req.getApellidos());
        }

        if (req.getTelefono() != null) {
            u.setTelefono(req.getTelefono());
        }

        if (req.getEmail() != null && !req.getEmail().equalsIgnoreCase(u.getEmail())) {
            if (usuarioRepo.existsByEmail(req.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya existe");
            }
            u.setEmail(req.getEmail());
        }

        return usuarioRepo.save(u);
    }
}
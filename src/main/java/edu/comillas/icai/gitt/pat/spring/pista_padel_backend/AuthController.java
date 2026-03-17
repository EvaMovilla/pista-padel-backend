package edu.comillas.icai.gitt.pat.spring.pista_padel_backend;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.LoginRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.MeResponse;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.RegisterRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Usuario;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.UsuarioRepositorio;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/pistaPadel/auth")
public class AuthController {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepositorio usuarioRepositorio,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {

        if (usuarioRepositorio.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("{\"error\":\"EMAIL_EXISTS\",\"message\":\"Email ya registrado\"}");
        }

        Usuario u = new Usuario();
        u.setNombre(request.getNombre());
        u.setApellidos(request.getApellidos());
        u.setEmail(request.getEmail());
        u.setTelefono(request.getTelefono());
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        u.setRol(Rol.USER);
        u.setFechaRegistro(LocalDateTime.now());
        u.setActivo(true);

        Usuario guardado = usuarioRepositorio.save(u);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("{\"idUsuario\":" + guardado.getIdUsuario() + "}");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {

            httpRequest.login(request.getEmail(), request.getPassword());
            HttpSession session = httpRequest.getSession(true);

            return ResponseEntity.ok("{\"message\":\"Login correcto\"}");
        } catch (ServletException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"BAD_CREDENTIALS\",\"message\":\"Credenciales incorrectas\"}");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario u = usuarioRepositorio.findByEmail(email).orElse(null);
        if (u == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"UNAUTHORIZED\",\"message\":\"Usuario no encontrado\"}");
        }

        return ResponseEntity.ok(new MeResponse(
                u.getIdUsuario(),
                u.getNombre(),
                u.getApellidos(),
                u.getEmail(),
                u.getTelefono(),
                u.getRol()
        ));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build(); // 204
    }

}

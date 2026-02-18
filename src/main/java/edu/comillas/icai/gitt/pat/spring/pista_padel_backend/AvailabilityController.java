package edu.comillas.icai.gitt.pat.spring.pista_padel_backend;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.EstadoReserva;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.PistaRepositorio;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.ReservaRepositorio;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pistaPadel")
public class AvailabilityController {

    private final PistaRepositorio pistaRepo;
    private final ReservaRepositorio reservaRepo;

    public AvailabilityController(PistaRepositorio pistaRepo, ReservaRepositorio reservaRepo) {
        this.pistaRepo = pistaRepo;
        this.reservaRepo = reservaRepo;
    }

    // courtId opcional (como guía)
    @GetMapping("/availability")
    public Map<String, Object> availability(
            @RequestParam("date") LocalDate date,
            @RequestParam(value = "courtId", required = false) Long courtId
    ) {
        if (date == null) {
            throw new RuntimeException("date es obligatorio");

        }

        List<Pista> pistas = (courtId == null)
                ? pistaRepo.findAll()
                : List.of(pistaRepo.findById(courtId).orElseThrow(() -> new RuntimeException("Pista no existe")));

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("date", date);

        var courts = pistas.stream().map(p -> Map.of(
                "courtId", p.getIdPista(),
                "nombre", p.getNombre(),
                "activa", p.isActiva(),
                "reservas", reservaRepo
                        .findByPista_IdPistaAndFechaReservaAndEstado(p.getIdPista(), date, EstadoReserva.ACTIVA)
                        .stream()
                        .map(r -> Map.of("inicio", r.getHoraInicio(), "fin", r.getHoraFin()))
                        .toList()
        )).toList();

        res.put("courts", courts);
        return res;
    }

    @GetMapping("/courts/{courtId}/availability")
    public Map<String, Object> availabilityCourt(
            @PathVariable Long courtId,
            @RequestParam("date") LocalDate date
    ) {
        return availability(date, courtId);
    }
}

package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Reserva;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservaRepositorio extends JpaRepository<Reserva, Long> {

    @Query("""
        select r from Reserva r
        where r.pista.idPista = :pistaId
          and r.fechaReserva = :fecha
          and r.estado = edu.comillas.icai.gitt.pat.spring.EstadoReserva.ACTIVA
          and (r.horaInicio < :fin and r.horaFin > :inicio)
    """)
    List<Reserva> findOverlaps(
            @Param("pistaId") Long pistaId,
            @Param("fecha") LocalDate fecha,
            @Param("inicio") LocalTime inicio,
            @Param("fin") LocalTime fin
    );

    List<Reserva> findByUsuarioIdUsuarioOrderByFechaReservaAscHoraInicioAsc(Long idUsuario);

    @Query("""
        select r from Reserva r
        where (:fecha is null or r.fechaReserva = :fecha)
          and (:pistaId is null or r.pista.idPista = :pistaId)
          and (:usuarioId is null or r.usuario.idUsuario = :usuarioId)
        order by r.fechaReserva asc, r.horaInicio asc
    """)
    List<Reserva> adminFilter(
            @Param("fecha") LocalDate fecha,
            @Param("pistaId") Long pistaId,
            @Param("usuarioId") Long usuarioId
    );
}

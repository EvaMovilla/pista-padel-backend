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

    // --- MÉTODOS VIRGINIA ---

    List<Reserva> findByUsuario_IdUsuarioAndFechaReservaBetween(
            Long idUsuario, LocalDate from, LocalDate to
    );

    List<Reserva> findByFechaReservaBetween(LocalDate from, LocalDate to);

    List<Reserva> findByEstadoAndFechaReservaBetween(
            EstadoReserva estado, LocalDate from, LocalDate to
    );

    List<Reserva> findByPista_IdPistaAndFechaReservaBetween(
            Long idPista, LocalDate from, LocalDate to
    );

    @Query("""
    SELECT r
    FROM Reserva r
    WHERE (:userId IS NULL OR r.usuario.idUsuario = :userId)
      AND (:courtId IS NULL OR r.pista.idPista = :courtId)
      AND (:estado IS NULL OR r.estado = :estado)
      AND (:from IS NULL OR r.fechaReserva >= :from)
      AND (:to IS NULL OR r.fechaReserva <= :to)
    ORDER BY r.fechaReserva DESC, r.horaInicio DESC
    """)
    List<Reserva> buscarConFiltros(
            @Param("userId") Long userId,
            @Param("courtId") Long courtId,
            @Param("estado") EstadoReserva estado,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    //Comprueba si existe alguna reserva ACTIVA que se solape con el horario que queremos
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reserva r " +
            "WHERE r.pista = :pista " +
            "AND r.fechaReserva = :fecha " +
            "AND r.estado = 'ACTIVA' " +
            "AND (r.horaInicio < :fin AND r.horaFin > :inicio)")
    boolean existeSolapamiento(@Param("pista") Pista pista,
                               @Param("fecha") LocalDate fecha,
                               @Param("inicio") LocalTime inicio,
                               @Param("fin") LocalTime fin);

    // --- MÉTODO EVA ---
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
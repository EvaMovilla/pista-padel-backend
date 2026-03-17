package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservaRepositorio extends JpaRepository<Reserva, Long> {

    // Para listar las reservas de un usuario
    List<Reserva> findByUsuario_IdUsuario(Long idUsuario);

    // Para buscar reservas activas de una pista en un día concreto (útil para disponibilidad)
    List<Reserva> findByPista_IdPistaAndFechaReservaAndEstado(Long idPista, LocalDate fechaReserva, EstadoReserva estado);


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
}

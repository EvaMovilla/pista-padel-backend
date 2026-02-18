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

    // LA MAGIA: Comprueba si existe alguna reserva ACTIVA que se solape con el horario que queremos
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

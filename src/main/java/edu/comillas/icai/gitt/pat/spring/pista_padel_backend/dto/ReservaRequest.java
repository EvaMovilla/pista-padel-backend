package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservaRequest {
    private Long courtId;
    private LocalDate date;
    private LocalTime startTime;
    private Integer durationMinutes;

    // Getters y Setters
    public Long getCourtId() { return courtId; }
    public void setCourtId(Long courtId) { this.courtId = courtId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
}
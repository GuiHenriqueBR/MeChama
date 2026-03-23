package com.mechama.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Horário disponível do prestador — retornado ao cliente na busca de agenda.
 */
@Data
@Builder
public class ScheduleResponse {
    private Long id;
    private Long providerProfileId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
    private LocalDateTime createdAt;
}

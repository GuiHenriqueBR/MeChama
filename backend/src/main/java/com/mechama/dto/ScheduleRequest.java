package com.mechama.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Payload para criação de um horário disponível pelo prestador.
 *
 * O prestador define o dia e os horários de início e fim.
 * O sistema valida que não há conflito com outros horários do prestador.
 */
public record ScheduleRequest(

        @NotNull(message = "Data é obrigatória")
        @FutureOrPresent(message = "Data deve ser hoje ou futura")
        LocalDate date,

        @NotNull(message = "Hora de início é obrigatória")
        LocalTime startTime,

        @NotNull(message = "Hora de término é obrigatória")
        LocalTime endTime
) {}

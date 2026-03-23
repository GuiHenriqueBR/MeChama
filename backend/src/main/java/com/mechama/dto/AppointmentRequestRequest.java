package com.mechama.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Payload para o cliente solicitar um agendamento.
 *
 * O cliente pode:
 * 1. Escolher um slot disponível (scheduleId) — recomendado.
 * 2. Propor uma data/hora livre (proposedDatetime) sem vincular a um slot.
 *
 * Ao menos proposedDatetime é obrigatório.
 * Se scheduleId for informado, o sistema valida que pertence ao prestador
 * e que ainda está disponível.
 */
public record AppointmentRequestRequest(

        @NotNull(message = "ID do prestador é obrigatório")
        Long providerUserId,

        @NotNull(message = "ID do serviço é obrigatório")
        Long serviceId,

        /** ID do slot de agenda (opcional — se o cliente escolheu da grade de horários) */
        Long scheduleId,

        @NotNull(message = "Data e hora proposta é obrigatória")
        @Future(message = "Data e hora devem ser no futuro")
        LocalDateTime proposedDatetime,

        /** Observações livres sobre o serviço (ex.: "apartamento no 3º andar, sem elevador") */
        String notes
) {}

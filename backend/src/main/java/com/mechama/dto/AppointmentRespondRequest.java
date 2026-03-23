package com.mechama.dto;

import com.mechama.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Payload para o prestador responder a uma solicitação de agendamento.
 *
 * Ações permitidas:
 * - CONFIRMED: prestador aceita o horário proposto pelo cliente
 * - REJECTED: prestador recusa a solicitação
 * - COUNTER_PROPOSED: prestador sugere outro horário (counterDatetime obrigatório)
 *
 * Validação de negócio no ScheduleService:
 * - Apenas CONFIRMED, REJECTED ou COUNTER_PROPOSED são aceitos aqui
 * - COUNTER_PROPOSED exige counterDatetime não nulo e no futuro
 */
public record AppointmentRespondRequest(

        @NotNull(message = "Ação de resposta é obrigatória")
        AppointmentStatus action,

        /** Obrigatório quando action = COUNTER_PROPOSED */
        LocalDateTime counterDatetime
) {}

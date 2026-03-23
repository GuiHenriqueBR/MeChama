package com.mechama.dto;

import com.mechama.model.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Resultado de uma solicitação de agendamento — retornado ao cliente e ao prestador.
 *
 * Integrações futuras:
 * - Tarefa 6 (Ordens): quando status = CONFIRMED, incluir orderId aqui
 * - Tarefa 8 (Avaliações): quando ordem COMPLETED, incluir link de avaliação
 */
@Data
@Builder
public class AppointmentRequestResponse {

    private Long id;

    // ─── Cliente ─────────────────────────────────────────────────────────────
    private Long clientId;
    private String clientName;

    // ─── Prestador ───────────────────────────────────────────────────────────
    private Long providerProfileId;
    private Long providerUserId;
    private String providerName;

    // ─── Serviço ─────────────────────────────────────────────────────────────
    private Long serviceId;
    private String serviceTitle;

    // ─── Agendamento ─────────────────────────────────────────────────────────
    private Long scheduleId;
    private LocalDateTime proposedDatetime;
    private AppointmentStatus status;

    /** Preenchido apenas quando status = COUNTER_PROPOSED */
    private LocalDateTime counterDatetime;

    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

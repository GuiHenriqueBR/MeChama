package com.mechama.model;

/**
 * Estados possíveis de uma solicitação de agendamento.
 *
 * Fluxo principal:
 *   PENDING → CONFIRMED (prestador aceita)
 *   PENDING → REJECTED (prestador recusa)
 *   PENDING → COUNTER_PROPOSED (prestador sugere outro horário)
 *   COUNTER_PROPOSED → CONFIRMED (cliente aceita a contraproposta)
 *   COUNTER_PROPOSED → REJECTED (cliente recusa a contraproposta)
 *
 * CONFIRMED é o gatilho para criação da Ordem de Serviço (Tarefa 6).
 */
public enum AppointmentStatus {
    PENDING,
    CONFIRMED,
    REJECTED,
    COUNTER_PROPOSED
}

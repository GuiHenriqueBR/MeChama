package com.mechama.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Solicitação de agendamento feita por um cliente a um prestador.
 *
 * O cliente propõe um horário; o prestador aceita, recusa ou
 * contra-propõe outro horário. Quando CONFIRMED, o módulo de
 * Ordens de Serviço (Tarefa 6) deve criar a Order correspondente.
 *
 * Relações:
 * - User (N:1) client — cliente que fez a solicitação
 * - ProviderProfile (N:1) — prestador que recebeu a solicitação
 * - ServiceOffering (N:1) — serviço solicitado
 * - Schedule (N:1, nullable) — slot de agenda vinculado (opcional)
 *
 * Integração futura:
 * - Tarefa 6 (Ordens): quando status = CONFIRMED, cria uma Order
 * - Tarefa 9 (Painel): exibido na agenda do prestador
 */
@Entity
@Table(name = "appointment_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cliente que está solicitando o serviço */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    /** Perfil do prestador que receberá o serviço */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_profile_id", nullable = false)
    private ProviderProfile providerProfile;

    /** Serviço que o cliente deseja contratar */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceOffering service;

    /**
     * Horário de agenda vinculado (opcional).
     * Pode ser null se o cliente propõe data livre sem usar um slot.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    /** Data e hora proposta pelo cliente */
    @Column(nullable = false)
    private LocalDateTime proposedDatetime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    /**
     * Horário alternativo sugerido pelo prestador (contraproposta).
     * Preenchido apenas quando status = COUNTER_PROPOSED.
     */
    @Column
    private LocalDateTime counterDatetime;

    /** Observações do cliente sobre o serviço solicitado */
    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

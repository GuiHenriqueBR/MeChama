package com.mechama.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Horário disponível cadastrado pelo prestador.
 *
 * O prestador define blocos de disponibilidade (data + hora início/fim).
 * Quando um AppointmentRequest é CONFIRMED para este horário,
 * available é marcado como false para evitar duplo agendamento.
 *
 * Relações:
 * - ProviderProfile (N:1) — prestador dono do horário
 *
 * Integração futura:
 * - Tarefa 9 (Painel do Prestador): exibido no calendário de atendimentos
 */
@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_profile_id", nullable = false)
    private ProviderProfile providerProfile;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * true = ainda disponível para agendamento.
     * Marcado false ao confirmar um AppointmentRequest para este slot.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean available = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}

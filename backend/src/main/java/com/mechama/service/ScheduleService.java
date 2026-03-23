package com.mechama.service;

import com.mechama.dto.ScheduleRequest;
import com.mechama.dto.ScheduleResponse;
import com.mechama.model.ProviderProfile;
import com.mechama.model.Schedule;
import com.mechama.repository.ProviderProfileRepository;
import com.mechama.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de gerenciamento de agenda do prestador.
 *
 * Responsabilidades:
 * 1. Prestador cadastra horários disponíveis.
 * 2. Clientes consultam horários disponíveis de um prestador.
 * 3. Prestador remove horários disponíveis.
 *
 * Regras de negócio:
 * - Horários no passado são rejeitados (validado no DTO).
 * - Conflito de horários no mesmo dia é bloqueado.
 * - Apenas o dono do horário pode removê-lo.
 * - Horários já vinculados a um AppointmentRequest CONFIRMED não podem ser removidos.
 *
 * Integração:
 * - AppointmentService: ao confirmar um agendamento, marca o Schedule.available = false.
 * - Tarefa 9 (Painel do Prestador): listagem via findByProvider com todos os horários.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ProviderProfileRepository providerProfileRepository;

    /**
     * Adiciona um horário disponível ao calendário do prestador.
     *
     * @param providerUserId userId do prestador autenticado (extraído do JWT)
     * @param request        data e horário de início/fim
     * @return horário criado
     */
    @Transactional
    public ScheduleResponse addSlot(Long providerUserId, ScheduleRequest request) {
        ProviderProfile profile = providerProfileRepository.findByUserId(providerUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Perfil de prestador não encontrado. Configure seu perfil antes de gerenciar a agenda."));

        if (request.endTime().isBefore(request.startTime()) || request.endTime().equals(request.startTime())) {
            throw new IllegalArgumentException("Hora de término deve ser após a hora de início.");
        }

        if (scheduleRepository.existsConflict(profile.getId(), request.date(), request.startTime(), request.endTime())) {
            throw new IllegalArgumentException(
                    "Conflito de horário: já existe um slot disponível neste período.");
        }

        Schedule schedule = Schedule.builder()
                .providerProfile(profile)
                .date(request.date())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .available(true)
                .build();

        Schedule saved = scheduleRepository.save(schedule);
        log.info("Schedule created: id={} provider={} date={} {}–{}",
                saved.getId(), providerUserId, saved.getDate(), saved.getStartTime(), saved.getEndTime());

        return toResponse(saved);
    }

    /**
     * Lista os horários disponíveis de um prestador a partir de hoje.
     * Endpoint público — usado pelo cliente ao escolher horário.
     *
     * @param providerUserId userId público do prestador
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAvailableSlots(Long providerUserId) {
        ProviderProfile profile = providerProfileRepository.findByUserId(providerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Prestador não encontrado."));

        return scheduleRepository
                .findAvailableByProvider(profile.getId(), LocalDate.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Lista todos os horários do próprio prestador (painel — inclui indisponíveis).
     *
     * @param providerUserId userId do prestador autenticado
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getMySlots(Long providerUserId) {
        ProviderProfile profile = providerProfileRepository.findByUserId(providerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado."));

        return scheduleRepository
                .findByProviderProfileIdOrderByDateAscStartTimeAsc(profile.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Remove um horário do prestador.
     *
     * @param providerUserId userId do prestador autenticado
     * @param scheduleId     id do horário a remover
     */
    @Transactional
    public void deleteSlot(Long providerUserId, Long scheduleId) {
        ProviderProfile profile = providerProfileRepository.findByUserId(providerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado."));

        if (!scheduleRepository.existsByIdAndProviderProfileId(scheduleId, profile.getId())) {
            throw new IllegalArgumentException("Horário não encontrado ou não pertence a este prestador.");
        }

        scheduleRepository.deleteById(scheduleId);
        log.info("Schedule deleted: id={} provider={}", scheduleId, providerUserId);
    }

    // ─── Mapeamento ──────────────────────────────────────────────────────────────

    ScheduleResponse toResponse(Schedule s) {
        return ScheduleResponse.builder()
                .id(s.getId())
                .providerProfileId(s.getProviderProfile().getId())
                .date(s.getDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .available(s.isAvailable())
                .createdAt(s.getCreatedAt())
                .build();
    }
}

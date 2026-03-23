package com.mechama.service;

import com.mechama.dto.AppointmentRequestRequest;
import com.mechama.dto.AppointmentRequestResponse;
import com.mechama.dto.AppointmentRespondRequest;
import com.mechama.model.*;
import com.mechama.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Lógica de solicitações de agendamento (cliente → prestador).
 *
 * Responsabilidades:
 * 1. Cliente cria uma solicitação de agendamento (PENDING).
 * 2. Prestador confirma, recusa ou contra-propõe horário.
 * 3. Ambos consultam suas solicitações paginadas.
 *
 * Regras de negócio:
 * - Apenas CLIENT pode criar uma solicitação.
 * - Apenas PROVIDER pode responder (confirmar/recusar/contra-propor).
 * - COUNTER_PROPOSED exige counterDatetime não nulo e futuro.
 * - Ao CONFIRMED com scheduleId, o Schedule.available é marcado false.
 * - Apenas solicitações em PENDING podem ser respondidas.
 *
 * Integração com Tarefa 6 (Ordens de Serviço):
 * - Quando status transita para CONFIRMED, o OrderService deve ser chamado
 *   para criar a Order correspondente. A integração será feita na Tarefa 6.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRequestRepository appointmentRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    /**
     * Cliente solicita um agendamento.
     *
     * @param clientUserId userId do cliente autenticado
     * @param request      dados da solicitação
     */
    @Transactional
    public AppointmentRequestResponse createRequest(Long clientUserId, AppointmentRequestRequest request) {
        User client = userRepository.findById(clientUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        ProviderProfile profile = providerProfileRepository.findByUserId(request.providerUserId())
                .orElseThrow(() -> new IllegalArgumentException("Prestador não encontrado."));

        if (!profile.isAvailable()) {
            throw new IllegalArgumentException("Prestador não está aceitando novos pedidos no momento.");
        }

        ServiceOffering service = serviceOfferingRepository.findById(request.serviceId())
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado."));

        if (!service.getProviderProfile().getId().equals(profile.getId())) {
            throw new IllegalArgumentException("Serviço não pertence a este prestador.");
        }

        if (!service.isActive()) {
            throw new IllegalArgumentException("Serviço não está disponível no momento.");
        }

        Schedule schedule = null;
        if (request.scheduleId() != null) {
            schedule = scheduleRepository.findById(request.scheduleId())
                    .orElseThrow(() -> new IllegalArgumentException("Horário não encontrado."));

            if (!schedule.getProviderProfile().getId().equals(profile.getId())) {
                throw new IllegalArgumentException("Horário não pertence a este prestador.");
            }

            if (!schedule.isAvailable()) {
                throw new IllegalArgumentException("Horário selecionado não está mais disponível.");
            }
        }

        AppointmentRequest appointment = AppointmentRequest.builder()
                .client(client)
                .providerProfile(profile)
                .service(service)
                .schedule(schedule)
                .proposedDatetime(request.proposedDatetime())
                .notes(request.notes())
                .status(AppointmentStatus.PENDING)
                .build();

        AppointmentRequest saved = appointmentRepository.save(appointment);
        log.info("AppointmentRequest created: id={} client={} provider={} service={}",
                saved.getId(), clientUserId, request.providerUserId(), request.serviceId());

        return toResponse(saved);
    }

    /**
     * Prestador responde a uma solicitação: confirma, recusa ou contra-propõe.
     *
     * @param providerUserId userId do prestador autenticado
     * @param appointmentId  id da solicitação a responder
     * @param request        ação e, se contra-proposta, novo horário
     */
    @Transactional
    public AppointmentRequestResponse respond(Long providerUserId, Long appointmentId,
                                              AppointmentRespondRequest request) {
        ProviderProfile profile = providerProfileRepository.findByUserId(providerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de prestador não encontrado."));

        AppointmentRequest appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação de agendamento não encontrada."));

        if (!appointment.getProviderProfile().getId().equals(profile.getId())) {
            throw new IllegalArgumentException("Solicitação não pertence a este prestador.");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Apenas solicitações com status PENDING podem ser respondidas. Status atual: "
                    + appointment.getStatus());
        }

        AppointmentStatus action = request.action();
        if (action != AppointmentStatus.CONFIRMED
                && action != AppointmentStatus.REJECTED
                && action != AppointmentStatus.COUNTER_PROPOSED) {
            throw new IllegalArgumentException(
                    "Ação inválida. Use CONFIRMED, REJECTED ou COUNTER_PROPOSED.");
        }

        if (action == AppointmentStatus.COUNTER_PROPOSED) {
            if (request.counterDatetime() == null) {
                throw new IllegalArgumentException(
                        "counterDatetime é obrigatório para COUNTER_PROPOSED.");
            }
            if (!request.counterDatetime().isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException(
                        "counterDatetime deve ser uma data e hora futuras.");
            }
            appointment.setCounterDatetime(request.counterDatetime());
        }

        appointment.setStatus(action);

        // Ao confirmar, bloqueia o slot de agenda se houver um vinculado
        if (action == AppointmentStatus.CONFIRMED && appointment.getSchedule() != null) {
            appointment.getSchedule().setAvailable(false);
            scheduleRepository.save(appointment.getSchedule());
        }

        AppointmentRequest updated = appointmentRepository.save(appointment);
        log.info("AppointmentRequest {} → {} by provider={}",
                appointmentId, action, providerUserId);

        // TODO Tarefa 6: se action == CONFIRMED, chamar OrderService.createFromAppointment(updated)

        return toResponse(updated);
    }

    /**
     * Lista solicitações do cliente autenticado.
     *
     * @param clientUserId userId do cliente
     * @param status       filtro por status (null = todos)
     * @param page         página (zero-indexed)
     * @param size         itens por página (máx. 20)
     */
    @Transactional(readOnly = true)
    public Page<AppointmentRequestResponse> listForClient(
            Long clientUserId, AppointmentStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 20), Sort.by("createdAt").descending());
        return appointmentRepository.findByClient(clientUserId, status, pageable)
                .map(this::toResponse);
    }

    /**
     * Lista solicitações recebidas pelo prestador autenticado.
     *
     * @param providerUserId userId do prestador
     * @param status         filtro por status (null = todos)
     * @param page           página (zero-indexed)
     * @param size           itens por página (máx. 20)
     */
    @Transactional(readOnly = true)
    public Page<AppointmentRequestResponse> listForProvider(
            Long providerUserId, AppointmentStatus status, int page, int size) {
        ProviderProfile profile = providerProfileRepository.findByUserId(providerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de prestador não encontrado."));

        PageRequest pageable = PageRequest.of(page, Math.min(size, 20), Sort.by("createdAt").descending());
        return appointmentRepository.findByProviderProfile(profile.getId(), status, pageable)
                .map(this::toResponse);
    }

    // ─── Mapeamento ──────────────────────────────────────────────────────────────

    private AppointmentRequestResponse toResponse(AppointmentRequest a) {
        return AppointmentRequestResponse.builder()
                .id(a.getId())
                .clientId(a.getClient().getId())
                .clientName(a.getClient().getName())
                .providerProfileId(a.getProviderProfile().getId())
                .providerUserId(a.getProviderProfile().getUser().getId())
                .providerName(a.getProviderProfile().getUser().getName())
                .serviceId(a.getService().getId())
                .serviceTitle(a.getService().getTitle())
                .scheduleId(a.getSchedule() != null ? a.getSchedule().getId() : null)
                .proposedDatetime(a.getProposedDatetime())
                .status(a.getStatus())
                .counterDatetime(a.getCounterDatetime())
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}

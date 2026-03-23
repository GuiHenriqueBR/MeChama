package com.mechama.controller;

import com.mechama.dto.AppointmentRequestRequest;
import com.mechama.dto.AppointmentRequestResponse;
import com.mechama.dto.AppointmentRespondRequest;
import com.mechama.model.AppointmentStatus;
import com.mechama.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de solicitação de agendamento (cliente solicita, prestador responde).
 *
 * Endpoints protegidos (CLIENT):
 *   POST /api/appointments                  — solicitar agendamento
 *   GET  /api/appointments/mine             — minhas solicitações como cliente
 *
 * Endpoints protegidos (PROVIDER):
 *   GET  /api/appointments/received         — solicitações recebidas pelo prestador
 *   PATCH /api/appointments/{id}/respond    — confirmar / recusar / contra-propor
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /** Cliente cria uma solicitação de agendamento */
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<AppointmentRequestResponse> createRequest(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody AppointmentRequestRequest request) {
        AppointmentRequestResponse response =
                appointmentService.createRequest(Long.parseLong(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Cliente lista suas próprias solicitações */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<AppointmentRequestResponse>> myRequests(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                appointmentService.listForClient(Long.parseLong(userId), status, page, size));
    }

    /** Prestador lista solicitações recebidas */
    @GetMapping("/received")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Page<AppointmentRequestResponse>> receivedRequests(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                appointmentService.listForProvider(Long.parseLong(userId), status, page, size));
    }

    /** Prestador responde a uma solicitação: CONFIRMED, REJECTED ou COUNTER_PROPOSED */
    @PatchMapping("/{appointmentId}/respond")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<AppointmentRequestResponse> respond(
            @AuthenticationPrincipal String userId,
            @PathVariable Long appointmentId,
            @Valid @RequestBody AppointmentRespondRequest request) {
        AppointmentRequestResponse response =
                appointmentService.respond(Long.parseLong(userId), appointmentId, request);
        return ResponseEntity.ok(response);
    }
}

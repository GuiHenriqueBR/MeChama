package com.mechama.controller;

import com.mechama.dto.ScheduleRequest;
import com.mechama.dto.ScheduleResponse;
import com.mechama.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de gerenciamento de agenda (horários disponíveis) do prestador.
 *
 * Endpoints públicos (sem JWT):
 *   GET  /api/schedule/provider/{userId}  — horários disponíveis de um prestador
 *
 * Endpoints protegidos (PROVIDER autenticado):
 *   GET    /api/schedule/mine             — todos os meus horários (painel)
 *   POST   /api/schedule                 — adicionar horário disponível
 *   DELETE /api/schedule/{id}            — remover horário
 */
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /** Horários disponíveis de um prestador — público (clientes veem ao agendar) */
    @GetMapping("/provider/{userId}")
    public ResponseEntity<List<ScheduleResponse>> getAvailableSlots(@PathVariable Long userId) {
        return ResponseEntity.ok(scheduleService.getAvailableSlots(userId));
    }

    /** Todos os meus horários — painel do prestador (inclui indisponíveis) */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<List<ScheduleResponse>> getMySlots(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(scheduleService.getMySlots(Long.parseLong(userId)));
    }

    /** Adiciona um horário disponível ao calendário */
    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ScheduleResponse> addSlot(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody ScheduleRequest request) {
        ScheduleResponse response = scheduleService.addSlot(Long.parseLong(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Remove um horário do calendário */
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Void> deleteSlot(
            @AuthenticationPrincipal String userId,
            @PathVariable Long scheduleId) {
        scheduleService.deleteSlot(Long.parseLong(userId), scheduleId);
        return ResponseEntity.noContent().build();
    }
}

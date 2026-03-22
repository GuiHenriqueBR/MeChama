package com.mechama.controller;

import com.mechama.dto.ServiceOfferingRequest;
import com.mechama.dto.ServiceOfferingResponse;
import com.mechama.service.ServiceOfferingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller do catálogo de serviços.
 *
 * Endpoints públicos (sem token):
 *   GET  /api/providers/{userId}/services         → serviços ativos de um prestador
 *   GET  /api/services/{serviceId}                → detalhe de um serviço
 *
 * Endpoints protegidos (JWT + ROLE_PROVIDER):
 *   GET    /api/providers/me/services             → todos os meus serviços (incl. inativos)
 *   POST   /api/providers/me/services             → criar serviço
 *   PUT    /api/providers/me/services/{id}        → atualizar serviço
 *   PATCH  /api/providers/me/services/{id}/toggle → ativar/desativar
 *   DELETE /api/providers/me/services/{id}        → excluir permanentemente
 */
@RestController
@RequiredArgsConstructor
public class ServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;

    // ─── Endpoints Públicos ──────────────────────────────────────────────────────

    @GetMapping("/api/providers/{userId}/services")
    public ResponseEntity<List<ServiceOfferingResponse>> getProviderServices(
            @PathVariable Long userId) {
        return ResponseEntity.ok(serviceOfferingService.getActiveServicesByProvider(userId));
    }

    @GetMapping("/api/services/{serviceId}")
    public ResponseEntity<ServiceOfferingResponse> getService(
            @PathVariable Long serviceId) {
        return ResponseEntity.ok(serviceOfferingService.getById(serviceId));
    }

    // ─── Endpoints do Prestador (autenticado) ───────────────────────────────────

    @GetMapping("/api/providers/me/services")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<List<ServiceOfferingResponse>> getMyServices(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(serviceOfferingService.getMyServices(Long.parseLong(userId)));
    }

    @PostMapping("/api/providers/me/services")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ServiceOfferingResponse> createService(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody ServiceOfferingRequest request) {
        ServiceOfferingResponse response = serviceOfferingService.create(Long.parseLong(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/providers/me/services/{serviceId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ServiceOfferingResponse> updateService(
            @AuthenticationPrincipal String userId,
            @PathVariable Long serviceId,
            @Valid @RequestBody ServiceOfferingRequest request) {
        return ResponseEntity.ok(
                serviceOfferingService.update(Long.parseLong(userId), serviceId, request));
    }

    @PatchMapping("/api/providers/me/services/{serviceId}/toggle")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ServiceOfferingResponse> toggleService(
            @AuthenticationPrincipal String userId,
            @PathVariable Long serviceId) {
        return ResponseEntity.ok(
                serviceOfferingService.toggleActive(Long.parseLong(userId), serviceId));
    }

    @DeleteMapping("/api/providers/me/services/{serviceId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Void> deleteService(
            @AuthenticationPrincipal String userId,
            @PathVariable Long serviceId) {
        serviceOfferingService.delete(Long.parseLong(userId), serviceId);
        return ResponseEntity.noContent().build();
    }
}

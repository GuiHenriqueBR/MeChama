package com.mechama.controller;

import com.mechama.dto.*;
import com.mechama.service.ProviderProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller de perfil de prestador e portfólio.
 *
 * Endpoints públicos (sem token):
 *   GET  /api/providers/{userId}/profile    — perfil completo do prestador
 *   GET  /api/providers/{userId}/portfolio  — itens de portfólio
 *
 * Endpoints protegidos (requerem JWT + ROLE_PROVIDER):
 *   PUT  /api/providers/me/profile          — atualiza perfil do prestador autenticado
 *   POST /api/providers/me/portfolio        — adiciona item ao portfólio
 *   DELETE /api/providers/me/portfolio/{id} — remove item do portfólio
 *
 * @AuthenticationPrincipal resolve para o userId (String) injetado pelo
 * JwtAuthenticationFilter — convertemos para Long no método.
 */
@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderProfileController {

    private final ProviderProfileService profileService;

    // ─── Endpoints Públicos ──────────────────────────────────────────────────────

    /**
     * Retorna o perfil público de um prestador.
     * Usado na tela de detalhe do prestador (clientes e visitantes).
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<ProviderProfileResponse> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    /**
     * Retorna os itens de portfólio de um prestador.
     */
    @GetMapping("/{userId}/portfolio")
    public ResponseEntity<List<PortfolioItemResponse>> getPortfolio(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getPortfolio(userId));
    }

    // ─── Endpoints Protegidos (somente PROVIDER autenticado) ────────────────────

    /**
     * Retorna o perfil do próprio prestador autenticado.
     */
    @GetMapping("/me/profile")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderProfileResponse> getMyProfile(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(profileService.getMyProfile(Long.parseLong(userId)));
    }

    /**
     * Cria ou atualiza o perfil do prestador autenticado.
     * PUT semântico: campos não enviados mantêm o valor anterior.
     */
    @PutMapping("/me/profile")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderProfileResponse> upsertProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody ProviderProfileRequest request) {
        ProviderProfileResponse response = profileService.upsertProfile(Long.parseLong(userId), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Adiciona um trabalho ao portfólio do prestador.
     */
    @PostMapping("/me/portfolio")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<PortfolioItemResponse> addPortfolioItem(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody PortfolioItemRequest request) {
        PortfolioItemResponse response = profileService.addPortfolioItem(Long.parseLong(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Remove um item do portfólio — somente o dono pode excluir.
     */
    @DeleteMapping("/me/portfolio/{itemId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Void> deletePortfolioItem(
            @AuthenticationPrincipal String userId,
            @PathVariable Long itemId) {
        profileService.deletePortfolioItem(Long.parseLong(userId), itemId);
        return ResponseEntity.noContent().build();
    }
}

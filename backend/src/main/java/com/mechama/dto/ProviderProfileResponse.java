package com.mechama.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Resposta pública do perfil do prestador.
 * Usada na tela de detalhe do prestador e nos resultados de busca (Tarefa 4).
 *
 * Tarefa 3: campo `services` adicionado — serviços ativos do prestador.
 * Integração futura (Tarefa 8): campo `recentReviews` (últimas avaliações).
 */
@Data
@Builder
public class ProviderProfileResponse {

    private Long id;

    // ─── Dados do User vinculado ────────────────────────────────────────────────
    private Long userId;
    private String userName;
    private String userEmail;

    // ─── Dados do perfil ────────────────────────────────────────────────────────
    private String bio;
    private String avatarUrl;
    private String city;
    private String neighborhood;
    private Integer experienceYears;
    private List<String> specialties;

    // ─── Reputação (atualizada pela Tarefa 8) ───────────────────────────────────
    private Double avgRating;
    private Integer totalReviews;

    // ─── Disponibilidade ────────────────────────────────────────────────────────
    private Integer serviceRadiusKm;
    private boolean available;

    // ─── Portfólio ──────────────────────────────────────────────────────────────
    private List<PortfolioItemResponse> portfolioItems;

    // ─── Serviços ativos (Tarefa 3) ─────────────────────────────────────────────
    private List<ServiceOfferingResponse> services;

    // ─── Avaliações recentes (Tarefa 8) ─────────────────────────────────────────
    // private List<ReviewResponse> recentReviews; // TODO na Tarefa 8

    private LocalDateTime updatedAt;
}

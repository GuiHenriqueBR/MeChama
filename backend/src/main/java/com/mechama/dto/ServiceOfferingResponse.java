package com.mechama.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resposta de serviço ofertado.
 *
 * Inclui dados do prestador (userId, name) para que telas de busca
 * (Tarefa 4) possam exibir o card do serviço sem precisar de uma
 * segunda chamada à API.
 *
 * Inclui dados da categoria para exibição de ícone e nome sem lookups extras.
 */
@Data
@Builder
public class ServiceOfferingResponse {

    private Long id;

    // ─── Prestador ───────────────────────────────────────────────────────────────
    private Long providerUserId;
    private String providerName;
    private String providerAvatarUrl;
    private Double providerAvgRating;
    private Integer providerTotalReviews;
    private String providerCity;

    // ─── Categoria ──────────────────────────────────────────────────────────────
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categorySlug;

    // ─── Serviço ────────────────────────────────────────────────────────────────
    private String title;
    private String description;
    private BigDecimal basePrice;
    private Integer durationMinutes;
    private String whatIsIncluded;
    private String whatIsNotIncluded;
    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

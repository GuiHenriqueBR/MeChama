package com.mechama.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Resultado único de uma busca — representa um serviço com seu prestador e score calculado.
 *
 * Mapeado a partir de SearchResultProjection (query nativa).
 *
 * O campo `score` é exposto ao cliente para eventual exibição de "destaque" ou badge
 * de relevância na UI. Permite ao mobile distinguir resultados de alta relevância.
 *
 * Integrações futuras:
 * - Tarefa 5 (Agendamento): campo `nextAvailableSlot` adicionado aqui
 * - Tarefa 8 (Avaliações): campo `recentReview` (snippet da última avaliação)
 * - Tarefa 11 (Anúncios): campo `isFeatured` quando `score` vier do componente 0.2
 */
@Data
@Builder
public class SearchResultResponse {

    // ─── Serviço ────────────────────────────────────────────────────────────────
    private Long serviceId;
    private String title;
    private String description;
    private BigDecimal basePrice;
    private Integer durationMinutes;
    private String whatIsIncluded;

    // ─── Prestador ──────────────────────────────────────────────────────────────
    private Long providerUserId;
    private Long providerProfileId;
    private String providerName;
    private String providerAvatarUrl;
    private Double avgRating;
    private Integer totalReviews;
    private String city;
    private String neighborhood;
    private Integer serviceRadiusKm;

    // ─── Categoria ──────────────────────────────────────────────────────────────
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categorySlug;

    // ─── Ranking ────────────────────────────────────────────────────────────────
    /** Score final (0.0 a 1.0) — soma ponderada de reputação, relevância, destaque e proximidade */
    private Double score;
}

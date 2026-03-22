package com.mechama.repository;

import java.math.BigDecimal;

/**
 * Projection para o resultado da query nativa de busca.
 *
 * Os aliases do SQL devem corresponder exatamente aos nomes dos métodos aqui.
 * A query usa aspas duplas (AS "avgRating") para preservar camelCase no PostgreSQL,
 * que por padrão converte identificadores para minúsculas.
 *
 * O campo `score` é calculado diretamente no SQL com a fórmula:
 *   score = (avgRating/5.0 * 0.4) + (textRelevance * 0.3) + (featured * 0.2) + (proximity * 0.1)
 *
 * - textRelevance: CASE WHEN com ILIKE (0.0 a 1.0)
 * - featured: sempre 0.0 — reservado para Tarefa 11 (Anúncios Patrocinados)
 * - proximity: city match booleano — reservado para coordenadas GPS futuras
 */
public interface SearchResultProjection {

    Long getServiceId();
    String getTitle();
    String getDescription();
    BigDecimal getBasePrice();
    Integer getDurationMinutes();
    String getWhatIsIncluded();

    Long getProfileId();
    Double getAvgRating();
    Integer getTotalReviews();
    String getCity();
    String getNeighborhood();
    String getAvatarUrl();
    Integer getServiceRadiusKm();

    Long getUserId();
    String getProviderName();

    Long getCategoryId();
    String getCategoryName();
    String getCategoryIcon();
    String getCategorySlug();

    /** Score calculado pelo banco — combina reputação, relevância textual e proximidade */
    Double getScore();
}

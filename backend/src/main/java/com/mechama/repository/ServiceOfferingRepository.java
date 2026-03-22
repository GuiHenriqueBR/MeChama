package com.mechama.repository;

import com.mechama.model.ServiceOffering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {

    /** Todos os serviços ativos de um prestador — usado no perfil público */
    List<ServiceOffering> findByProviderProfileIdAndActiveTrueOrderByCreatedAtDesc(Long providerProfileId);

    /** Todos os serviços de um prestador (para o painel dele — inclui inativos) */
    List<ServiceOffering> findByProviderProfileIdOrderByCreatedAtDesc(Long providerProfileId);

    /** Garante que o serviço pertence ao prestador antes de editar/deletar */
    boolean existsByIdAndProviderProfileId(Long serviceId, Long providerProfileId);

    /**
     * Busca paginada por categoria — base legada (ainda usada internamente).
     * A busca pública usa searchServices() abaixo com score multi-critério.
     */
    @Query("""
        SELECT s FROM ServiceOffering s
        WHERE s.active = true
          AND s.providerProfile.available = true
          AND (:categoryId IS NULL OR s.category.id = :categoryId)
        ORDER BY s.providerProfile.avgRating DESC
        """)
    Page<ServiceOffering> findActiveByCategory(
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    /**
     * Busca multi-critério com score calculado em banco.
     *
     * Score formula (0 a 1):
     *   (avgRating/5.0 * 0.4)    — reputação do prestador
     *   (textRelevance  * 0.3)   — relevância textual (ILIKE em título, descrição, categoria)
     *   (featured       * 0.2)   — destaque pago — sempre 0.0, reservado para Tarefa 11
     *   (proximityScore * 0.1)   — proximidade por cidade (booleano; GPS na Fase 2)
     */
    @Query(
        value = """
            SELECT
                so.id                AS "serviceId",
                so.title             AS "title",
                so.description       AS "description",
                so.base_price        AS "basePrice",
                so.duration_minutes  AS "durationMinutes",
                so.what_is_included  AS "whatIsIncluded",
                pp.id                AS "profileId",
                pp.avg_rating        AS "avgRating",
                pp.total_reviews     AS "totalReviews",
                pp.city              AS "city",
                pp.neighborhood      AS "neighborhood",
                pp.avatar_url        AS "avatarUrl",
                pp.service_radius_km AS "serviceRadiusKm",
                u.id                 AS "userId",
                u.name               AS "providerName",
                sc.id                AS "categoryId",
                sc.name              AS "categoryName",
                sc.icon              AS "categoryIcon",
                sc.slug              AS "categorySlug",
                (
                    (pp.avg_rating / 5.0) * 0.4
                    + CASE
                        WHEN :q IS NULL OR CAST(:q AS TEXT) = ''
                            THEN 0.3
                        WHEN LOWER(so.title) = LOWER(CAST(:q AS TEXT))
                            THEN 0.3
                        WHEN LOWER(so.title) LIKE LOWER(CONCAT('%', CAST(:q AS TEXT), '%'))
                            THEN 0.21
                        WHEN LOWER(so.description) LIKE LOWER(CONCAT('%', CAST(:q AS TEXT), '%'))
                            THEN 0.12
                        WHEN LOWER(sc.name) LIKE LOWER(CONCAT('%', CAST(:q AS TEXT), '%'))
                            THEN 0.09
                        ELSE 0.0
                      END
                    + 0.0
                    + CASE
                        WHEN :city IS NULL OR CAST(:city AS TEXT) = '' THEN 0.0
                        WHEN LOWER(pp.city) = LOWER(CAST(:city AS TEXT)) THEN 0.1
                        ELSE 0.0
                      END
                ) AS "score"
            FROM service_offerings so
            JOIN provider_profiles pp ON so.provider_profile_id = pp.id
            JOIN users u              ON pp.user_id = u.id
            JOIN service_categories sc ON so.category_id = sc.id
            WHERE so.active = true
              AND pp.available = true
              AND u.active = true
              AND (
                    :q IS NULL OR CAST(:q AS TEXT) = ''
                    OR LOWER(so.title) LIKE LOWER(CONCAT('%', CAST(:q AS TEXT), '%'))
                    OR LOWER(so.description) LIKE LOWER(CONCAT('%', CAST(:q AS TEXT), '%'))
                    OR LOWER(sc.name) LIKE LOWER(CONCAT('%', CAST(:q AS TEXT), '%'))
                  )
              AND (:categoryId IS NULL OR sc.id = :categoryId)
              AND (
                    :city IS NULL OR CAST(:city AS TEXT) = ''
                    OR LOWER(pp.city) LIKE LOWER(CONCAT('%', CAST(:city AS TEXT), '%'))
                  )
              AND (:minRating IS NULL OR pp.avg_rating >= CAST(:minRating AS DOUBLE PRECISION))
              AND (:maxPrice IS NULL OR so.base_price <= CAST(:maxPrice AS NUMERIC))
              AND (:minPrice IS NULL OR so.base_price >= CAST(:minPrice AS NUMERIC))
            ORDER BY "score" DESC
            """,
        countQuery = """
            SELECT COUNT(*)
            FROM service_offerings so
            JOIN provider_profiles pp ON so.provider_profile_id = pp.id
            JOIN users u              ON pp.user_id = u.id
            JOIN service_categories sc ON so.category_id = sc.id
            WHERE so.active = true
              AND pp.available = true
              AND u.active = true
              AND (
                    :q IS NULL OR CAST(:q AS TEXT) = ''
                    OR LOWER(so.title) LIKE LOWER(CONCAT('%', CAST(:q AS TEXT), '%'))
                    OR LOWER(so.description) LIKE LOWER(CONCAT('%', CAST(:q AS TEXT), '%'))
                    OR LOWER(sc.name) LIKE LOWER(CONCAT('%', CAST(:q AS TEXT), '%'))
                  )
              AND (:categoryId IS NULL OR sc.id = :categoryId)
              AND (
                    :city IS NULL OR CAST(:city AS TEXT) = ''
                    OR LOWER(pp.city) LIKE LOWER(CONCAT('%', CAST(:city AS TEXT), '%'))
                  )
              AND (:minRating IS NULL OR pp.avg_rating >= CAST(:minRating AS DOUBLE PRECISION))
              AND (:maxPrice IS NULL OR so.base_price <= CAST(:maxPrice AS NUMERIC))
              AND (:minPrice IS NULL OR so.base_price >= CAST(:minPrice AS NUMERIC))
            """,
        nativeQuery = true
    )
    Page<SearchResultProjection> searchServices(
            @Param("q") String q,
            @Param("categoryId") Long categoryId,
            @Param("city") String city,
            @Param("minRating") Double minRating,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minPrice") BigDecimal minPrice,
            Pageable pageable);
}

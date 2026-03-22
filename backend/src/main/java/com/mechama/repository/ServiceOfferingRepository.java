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
}

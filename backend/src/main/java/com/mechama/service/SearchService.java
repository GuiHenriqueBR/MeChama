package com.mechama.service;

import com.mechama.dto.SearchResultResponse;
import com.mechama.repository.SearchResultProjection;
import com.mechama.repository.ServiceOfferingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Lógica de busca e descoberta de serviços — Tarefa 4.
 *
 * Responsabilidades:
 * 1. Receber e normalizar parâmetros de busca (null-safe, strings em branco → null).
 * 2. Delegar a query nativa de score multi-critério ao ServiceOfferingRepository.
 * 3. Mapear SearchResultProjection → SearchResultResponse.
 *
 * Score calculado em banco (fórmula completa em ServiceOfferingRepository.searchServices):
 *   reputação    × 0.4   — avgRating normalizado (÷ 5.0)
 *   relevância   × 0.3   — match textual por ILIKE (título > descrição > categoria)
 *   destaque     × 0.2   — sempre 0.0 até Tarefa 11 (Anúncios)
 *   proximidade  × 0.1   — match de cidade (booleano; GPS na Fase 2)
 *
 * Integrações com módulos existentes:
 * - ProviderProfile (T2): avgRating, city, avatarUrl usados no resultado
 * - ServiceOffering (T3): título, preço, categoria — unidade de resultado
 *
 * Integrações com módulos futuros:
 * - Tarefa 8 (Avaliações): avgRating será atualizado em tempo real
 * - Tarefa 9 (Painel): available = false remove o prestador dos resultados
 * - Tarefa 11 (Anúncios): featured score substituirá o 0.0 no cálculo
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

    private static final int MAX_PAGE_SIZE = 20;

    private final ServiceOfferingRepository serviceOfferingRepository;

    /**
     * Executa a busca multi-critério e retorna uma página de resultados ranqueados.
     *
     * @param q          Texto livre (busca em título, descrição e nome de categoria)
     * @param categoryId Filtro por categoria (null = todas)
     * @param city       Filtro por cidade do prestador (null = todas)
     * @param minRating  Avaliação média mínima, ex: 4.0 (null = sem restrição)
     * @param maxPrice   Preço máximo em BRL (null = sem restrição)
     * @param minPrice   Preço mínimo em BRL (null = sem restrição)
     * @param page       Página (zero-indexed)
     * @param size       Itens por página (máximo 20, forçado aqui)
     */
    @Transactional(readOnly = true)
    public Page<SearchResultResponse> search(
            String q,
            Long categoryId,
            String city,
            Double minRating,
            BigDecimal maxPrice,
            BigDecimal minPrice,
            int page,
            int size) {

        String normalizedQ    = blankToNull(q);
        String normalizedCity = blankToNull(city);
        int    safeSize       = Math.min(size, MAX_PAGE_SIZE);

        log.info("Search: q='{}' category={} city='{}' minRating={} price=[{},{}] page={}/{}",
                normalizedQ, categoryId, normalizedCity, minRating, minPrice, maxPrice, page, safeSize);

        // Sort.unsorted() garante que o Pageable não sobrescreva o ORDER BY score DESC do SQL
        PageRequest pageable = PageRequest.of(page, safeSize, Sort.unsorted());

        return serviceOfferingRepository
                .searchServices(normalizedQ, categoryId, normalizedCity,
                        minRating, maxPrice, minPrice, pageable)
                .map(this::toResponse);
    }

    // ─── Mapeamento ──────────────────────────────────────────────────────────────

    private SearchResultResponse toResponse(SearchResultProjection p) {
        return SearchResultResponse.builder()
                // Serviço
                .serviceId(p.getServiceId())
                .title(p.getTitle())
                .description(p.getDescription())
                .basePrice(p.getBasePrice())
                .durationMinutes(p.getDurationMinutes())
                .whatIsIncluded(p.getWhatIsIncluded())
                // Prestador
                .providerUserId(p.getUserId())
                .providerProfileId(p.getProfileId())
                .providerName(p.getProviderName())
                .providerAvatarUrl(p.getAvatarUrl())
                .avgRating(p.getAvgRating())
                .totalReviews(p.getTotalReviews())
                .city(p.getCity())
                .neighborhood(p.getNeighborhood())
                .serviceRadiusKm(p.getServiceRadiusKm())
                // Categoria
                .categoryId(p.getCategoryId())
                .categoryName(p.getCategoryName())
                .categoryIcon(p.getCategoryIcon())
                .categorySlug(p.getCategorySlug())
                // Score
                .score(p.getScore())
                .build();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}

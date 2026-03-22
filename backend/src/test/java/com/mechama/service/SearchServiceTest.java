package com.mechama.service;

import com.mechama.dto.SearchResultResponse;
import com.mechama.repository.SearchResultProjection;
import com.mechama.repository.ServiceOfferingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private ServiceOfferingRepository serviceOfferingRepository;

    @InjectMocks
    private SearchService searchService;

    private SearchResultProjection projection;

    @BeforeEach
    void setUp() {
        projection = mockProjection(
                1L, "Instalação Elétrica", "Instalação de tomadas e disjuntores",
                new BigDecimal("150.00"), 120, "Mão de obra + testes",
                10L, 4.8, 42, "São Paulo", "Moema", "https://avatar.url", 30,
                5L, "João Elétrico",
                2L, "Elétrica", "⚡", "eletrica",
                0.87
        );
    }

    // ─── Busca básica ─────────────────────────────────────────────────────────

    @Test
    void shouldReturnPagedResultsWhenSearchIsSuccessful() {
        Page<SearchResultProjection> repoPage = new PageImpl<>(List.of(projection));
        when(serviceOfferingRepository.searchServices(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(repoPage);

        Page<SearchResultResponse> result = searchService.search(
                "elétrica", null, null, null, null, null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Instalação Elétrica");
        assertThat(result.getContent().get(0).getScore()).isEqualTo(0.87);
    }

    @Test
    void shouldReturnEmptyPageWhenNoResultsFound() {
        Page<SearchResultProjection> emptyPage = new PageImpl<>(List.of());
        when(serviceOfferingRepository.searchServices(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

        Page<SearchResultResponse> result = searchService.search(
                "serviço inexistente", null, null, null, null, null, 0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // ─── Normalização de parâmetros ───────────────────────────────────────────

    @Test
    void shouldConvertBlankQueryToNull() {
        Page<SearchResultProjection> emptyPage = new PageImpl<>(List.of());
        when(serviceOfferingRepository.searchServices(
                isNull(), any(), any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

        searchService.search("   ", null, null, null, null, null, 0, 10);

        verify(serviceOfferingRepository).searchServices(
                isNull(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldTrimQueryBeforePassingToRepository() {
        Page<SearchResultProjection> emptyPage = new PageImpl<>(List.of());
        when(serviceOfferingRepository.searchServices(
                eq("eletricista"), any(), any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

        searchService.search("  eletricista  ", null, null, null, null, null, 0, 10);

        verify(serviceOfferingRepository).searchServices(
                eq("eletricista"), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldConvertBlankCityToNull() {
        Page<SearchResultProjection> emptyPage = new PageImpl<>(List.of());
        when(serviceOfferingRepository.searchServices(
                any(), any(), isNull(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

        searchService.search(null, null, "   ", null, null, null, 0, 10);

        verify(serviceOfferingRepository).searchServices(
                any(), any(), isNull(), any(), any(), any(), any());
    }

    // ─── Limite máximo de página ──────────────────────────────────────────────

    @Test
    void shouldCapPageSizeAt20WhenRequestedSizeExceedsLimit() {
        Page<SearchResultProjection> emptyPage = new PageImpl<>(List.of());
        when(serviceOfferingRepository.searchServices(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

        searchService.search(null, null, null, null, null, null, 0, 100);

        verify(serviceOfferingRepository).searchServices(
                any(), any(), any(), any(), any(), any(),
                argThat(pageable -> pageable.getPageSize() == 20));
    }

    @Test
    void shouldUseRequestedSizeWhenBelowLimit() {
        Page<SearchResultProjection> emptyPage = new PageImpl<>(List.of());
        when(serviceOfferingRepository.searchServices(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

        searchService.search(null, null, null, null, null, null, 0, 5);

        verify(serviceOfferingRepository).searchServices(
                any(), any(), any(), any(), any(), any(),
                argThat(pageable -> pageable.getPageSize() == 5));
    }

    // ─── Mapeamento de campos ─────────────────────────────────────────────────

    @Test
    void shouldMapAllProjectionFieldsToResponse() {
        Page<SearchResultProjection> repoPage = new PageImpl<>(List.of(projection));
        when(serviceOfferingRepository.searchServices(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(repoPage);

        SearchResultResponse response = searchService
                .search(null, null, null, null, null, null, 0, 10)
                .getContent().get(0);

        // Serviço
        assertThat(response.getServiceId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Instalação Elétrica");
        assertThat(response.getDescription()).isEqualTo("Instalação de tomadas e disjuntores");
        assertThat(response.getBasePrice()).isEqualByComparingTo("150.00");
        assertThat(response.getDurationMinutes()).isEqualTo(120);
        assertThat(response.getWhatIsIncluded()).isEqualTo("Mão de obra + testes");

        // Prestador
        assertThat(response.getProviderProfileId()).isEqualTo(10L);
        assertThat(response.getAvgRating()).isEqualTo(4.8);
        assertThat(response.getTotalReviews()).isEqualTo(42);
        assertThat(response.getCity()).isEqualTo("São Paulo");
        assertThat(response.getNeighborhood()).isEqualTo("Moema");
        assertThat(response.getProviderAvatarUrl()).isEqualTo("https://avatar.url");
        assertThat(response.getServiceRadiusKm()).isEqualTo(30);
        assertThat(response.getProviderUserId()).isEqualTo(5L);
        assertThat(response.getProviderName()).isEqualTo("João Elétrico");

        // Categoria
        assertThat(response.getCategoryId()).isEqualTo(2L);
        assertThat(response.getCategoryName()).isEqualTo("Elétrica");
        assertThat(response.getCategoryIcon()).isEqualTo("⚡");
        assertThat(response.getCategorySlug()).isEqualTo("eletrica");

        // Score
        assertThat(response.getScore()).isEqualTo(0.87);
    }

    @Test
    void shouldPassAllFiltersToRepository() {
        Page<SearchResultProjection> emptyPage = new PageImpl<>(List.of());
        when(serviceOfferingRepository.searchServices(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

        searchService.search("limpeza", 3L, "Curitiba", 4.0, new BigDecimal("500"), new BigDecimal("50"), 2, 10);

        verify(serviceOfferingRepository).searchServices(
                eq("limpeza"),
                eq(3L),
                eq("Curitiba"),
                eq(4.0),
                eq(new BigDecimal("500")),
                eq(new BigDecimal("50")),
                argThat(p -> p.getPageNumber() == 2 && p.getPageSize() == 10)
        );
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    @SuppressWarnings("SameParameterValue")
    private SearchResultProjection mockProjection(
            Long serviceId, String title, String description,
            BigDecimal basePrice, Integer durationMinutes, String whatIsIncluded,
            Long profileId, Double avgRating, Integer totalReviews,
            String city, String neighborhood, String avatarUrl, Integer serviceRadiusKm,
            Long userId, String providerName,
            Long categoryId, String categoryName, String categoryIcon, String categorySlug,
            Double score) {

        return new SearchResultProjection() {
            public Long getServiceId()        { return serviceId; }
            public String getTitle()          { return title; }
            public String getDescription()    { return description; }
            public BigDecimal getBasePrice()  { return basePrice; }
            public Integer getDurationMinutes(){ return durationMinutes; }
            public String getWhatIsIncluded() { return whatIsIncluded; }
            public Long getProfileId()        { return profileId; }
            public Double getAvgRating()      { return avgRating; }
            public Integer getTotalReviews()  { return totalReviews; }
            public String getCity()           { return city; }
            public String getNeighborhood()   { return neighborhood; }
            public String getAvatarUrl()      { return avatarUrl; }
            public Integer getServiceRadiusKm(){ return serviceRadiusKm; }
            public Long getUserId()           { return userId; }
            public String getProviderName()   { return providerName; }
            public Long getCategoryId()       { return categoryId; }
            public String getCategoryName()   { return categoryName; }
            public String getCategoryIcon()   { return categoryIcon; }
            public String getCategorySlug()   { return categorySlug; }
            public Double getScore()          { return score; }
        };
    }
}

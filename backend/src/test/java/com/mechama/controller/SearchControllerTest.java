package com.mechama.controller;

import com.mechama.dto.SearchResultResponse;
import com.mechama.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração do SearchController.
 *
 * Verifica que:
 * - O endpoint é acessível sem token (público)
 * - Parâmetros de query são repassados corretamente ao SearchService
 * - Paginação é respeitada
 * - Resposta JSON contém os campos esperados
 */
@WebMvcTest(controllers = SearchController.class)
@Import(TestSecurityConfig.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    // ─── Endpoint acessível sem autenticação ──────────────────────────────────

    @Test
    void shouldReturn200WithoutAuthentication() throws Exception {
        when(searchService.search(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/search"))
                .andExpect(status().isOk());
    }

    // ─── Resposta paginada ────────────────────────────────────────────────────

    @Test
    void shouldReturnPagedResultsInResponse() throws Exception {
        SearchResultResponse item = SearchResultResponse.builder()
                .serviceId(1L)
                .title("Instalação Elétrica")
                .providerName("João Silva")
                .avgRating(4.8)
                .totalReviews(42)
                .basePrice(new BigDecimal("150.00"))
                .categoryName("Elétrica")
                .categorySlug("eletrica")
                .score(0.87)
                .build();

        Page<SearchResultResponse> page = new PageImpl<>(List.of(item));
        when(searchService.search(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/search").param("q", "eletricista"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].serviceId").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Instalação Elétrica"))
                .andExpect(jsonPath("$.content[0].providerName").value("João Silva"))
                .andExpect(jsonPath("$.content[0].score").value(0.87))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturnEmptyPageWhenNoResults() throws Exception {
        when(searchService.search(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/search").param("q", "xyz_nao_existe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    // ─── Repasse de parâmetros ao service ────────────────────────────────────

    @Test
    void shouldPassAllQueryParamsToService() throws Exception {
        when(searchService.search(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/search")
                .param("q", "limpeza")
                .param("categoryId", "3")
                .param("city", "São Paulo")
                .param("minRating", "4.0")
                .param("minPrice", "50")
                .param("maxPrice", "500")
                .param("page", "1")
                .param("size", "15"))
                .andExpect(status().isOk());

        verify(searchService).search(
                eq("limpeza"),
                eq(3L),
                eq("São Paulo"),
                eq(4.0),
                eq(new BigDecimal("500")),
                eq(new BigDecimal("50")),
                eq(1),
                eq(15)
        );
    }

    @Test
    void shouldUseDefaultPaginationWhenParamsAreAbsent() throws Exception {
        when(searchService.search(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/search"))
                .andExpect(status().isOk());

        verify(searchService).search(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(10)
        );
    }

    @Test
    void shouldHandleSearchWithOnlyQueryParam() throws Exception {
        when(searchService.search(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/search").param("q", "pintor"))
                .andExpect(status().isOk());

        verify(searchService).search(
                eq("pintor"), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(10)
        );
    }
}

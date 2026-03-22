package com.mechama.controller;

import com.mechama.dto.SearchResultResponse;
import com.mechama.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Endpoint público de busca e descoberta — Tarefa 4.
 *
 * GET /api/search
 *
 * Parâmetros de query (todos opcionais):
 *   q          — texto livre (título, descrição, categoria)
 *   categoryId — filtrar por categoria
 *   city       — filtrar por cidade do prestador
 *   minRating  — avaliação mínima (ex: 4.0)
 *   maxPrice   — preço máximo em BRL
 *   minPrice   — preço mínimo em BRL
 *   page       — página (padrão: 0)
 *   size       — itens por página (padrão: 10, máximo: 20)
 *
 * Endpoint público — não requer JWT.
 * Adicionar autenticação opcional no futuro para personalizar resultados por perfil.
 *
 * Exemplo de chamada:
 *   GET /api/search?q=eletricista&city=São Paulo&minRating=4.0&page=0&size=10
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<Page<SearchResultResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<SearchResultResponse> results = searchService.search(
                q, categoryId, city, minRating, maxPrice, minPrice, page, size);

        return ResponseEntity.ok(results);
    }
}

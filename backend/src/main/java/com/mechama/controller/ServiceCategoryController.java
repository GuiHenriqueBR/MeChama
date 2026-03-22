package com.mechama.controller;

import com.mechama.dto.ServiceCategoryResponse;
import com.mechama.service.ServiceOfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint público de categorias de serviço.
 *
 * GET /api/categories → lista todas as categorias ativas com preço mínimo.
 * Usado no formulário de cadastro de serviço (seletor de categoria no app).
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class ServiceCategoryController {

    private final ServiceOfferingService serviceOfferingService;

    @GetMapping
    public ResponseEntity<List<ServiceCategoryResponse>> listCategories() {
        return ResponseEntity.ok(serviceOfferingService.listCategories());
    }
}

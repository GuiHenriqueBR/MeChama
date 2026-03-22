package com.mechama.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Resposta de categoria de serviço.
 * Usada no formulário de criação de serviço no app
 * para popular o seletor de categorias com o preço mínimo visível.
 */
@Data
@Builder
public class ServiceCategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String icon;
    private BigDecimal minPrice;
}

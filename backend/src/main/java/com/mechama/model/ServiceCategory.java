package com.mechama.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Categorias de serviço predefinidas da plataforma.
 *
 * Cada categoria define:
 * - Um ícone (emoji ou nome de ícone) para exibição no app
 * - Um preço mínimo (floor): serviços não podem ser cadastrados abaixo desse valor
 *   para evitar dumping de preço (regra de negócio crítica do CLAUDE.md)
 *
 * Integração futura (Tarefa 4 — Busca):
 * - O filtro de categoria na busca usa o campo `slug` para queries limpas.
 * - O preço mínimo por categoria poderá ser ajustado por IA com base no
 *   histórico de preços e oferta/demanda da plataforma.
 */
@Entity
@Table(name = "service_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome legível: "Elétrica", "Hidráulica", "Limpeza", etc. */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Slug para uso em filtros de URL: "eletrica", "hidraulica", etc.
     * Gerado automaticamente no DataInitializer.
     */
    @Column(nullable = false, unique = true)
    private String slug;

    /** Emoji ou identificador de ícone para o app mobile */
    @Column
    private String icon;

    /**
     * Preço mínimo por hora/sessão nessa categoria.
     * Serviços abaixo desse valor são rejeitados na criação/edição.
     * Padrão: R$ 0 (sem restrição inicial — IA ajustará conforme dados).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minPrice = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<ServiceOffering> services = new ArrayList<>();
}

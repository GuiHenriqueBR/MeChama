package com.mechama.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Serviço ofertado por um prestador.
 *
 * Cada PROVIDER pode ter N serviços ativos em sua vitrine.
 *
 * Relações:
 * - ProviderProfile (N:1) — prestador dono do serviço
 * - ServiceCategory (N:1) — categoria que define o preço mínimo e os filtros de busca
 * - Order (1:N) — ordens de serviço geradas a partir deste serviço (Tarefa 6)
 *
 * Regra crítica de negócio:
 * - basePrice NUNCA pode ser menor que category.minPrice
 *   (validado no ServiceOfferingService antes de persistir)
 */
@Entity
@Table(name = "service_offerings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_profile_id", nullable = false)
    private ProviderProfile providerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory category;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Preço base do serviço (em BRL).
     * Deve ser >= category.minPrice.
     * Será a base para o cálculo do escrow na Tarefa 7.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    /** Tempo estimado de execução em minutos */
    @Column
    private Integer durationMinutes;

    /**
     * Texto livre descrevendo o que está incluso no serviço.
     * Ex.: "Instalação completa, teste de funcionamento, garantia de 90 dias"
     */
    @Column(columnDefinition = "TEXT")
    private String whatIsIncluded;

    /**
     * Itens explicitamente excluídos do escopo.
     * Ex.: "Materiais, deslocamento acima de 20km"
     */
    @Column(columnDefinition = "TEXT")
    private String whatIsNotIncluded;

    /** false = desativado temporariamente pelo prestador (sem aparecer na busca) */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

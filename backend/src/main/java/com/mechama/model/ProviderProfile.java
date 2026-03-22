package com.mechama.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Perfil público do prestador de serviços.
 *
 * Relações:
 * - User (1:1) — criado automaticamente quando type == PROVIDER é cadastrado
 * - PortfolioItem (1:N) — trabalhos realizados pelo prestador
 * - ServiceOffering (1:N) — serviços ofertados (Tarefa 3)
 * - Review (1:N) — avaliações recebidas (Tarefa 8)
 *
 * avgRating e totalReviews são atualizados pela Tarefa 8 (avaliações)
 * e influenciam diretamente a ordenação na busca (Tarefa 4).
 */
@Entity
@Table(name = "provider_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Relação 1:1 com User. Foreign key: user_id */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Texto de apresentação do prestador */
    @Column(columnDefinition = "TEXT")
    private String bio;

    /** URL da foto de perfil (AWS S3 no futuro) */
    @Column
    private String avatarUrl;

    @Column
    private String city;

    @Column
    private String neighborhood;

    /** Anos de experiência na(s) especialidade(s) */
    @Column
    private Integer experienceYears;

    /**
     * Lista de especialidades como array de strings.
     * Ex.: ["Elétrica residencial", "Instalação de painéis solares"]
     * Armazenado como texto separado por vírgula no banco.
     * Migrar para tabela separada se precisar de filtragem avançada (Tarefa 4).
     */
    @ElementCollection
    @CollectionTable(
        name = "provider_specialties",
        joinColumns = @JoinColumn(name = "provider_profile_id")
    )
    @Column(name = "specialty")
    @Builder.Default
    private List<String> specialties = new ArrayList<>();

    /**
     * Média de avaliações (1.0 a 5.0).
     * Calculada e atualizada pelo módulo de avaliações (Tarefa 8).
     * Influencia o score de ordenação na busca (Tarefa 4).
     */
    @Column(nullable = false)
    @Builder.Default
    private Double avgRating = 0.0;

    /** Quantidade total de avaliações recebidas */
    @Column(nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    /**
     * Raio de atendimento em km.
     * Usado no filtro de distância da busca (Tarefa 4).
     * Prestador configura no painel (Tarefa 9).
     */
    @Column
    @Builder.Default
    private Integer serviceRadiusKm = 20;

    /** true = aceita novos pedidos; false = offline (configurável no painel - Tarefa 9) */
    @Column(nullable = false)
    @Builder.Default
    private boolean available = true;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ─── Portfólio ──────────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "providerProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PortfolioItem> portfolioItems = new ArrayList<>();
}

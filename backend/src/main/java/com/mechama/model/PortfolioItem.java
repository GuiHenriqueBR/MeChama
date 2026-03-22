package com.mechama.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Item do portfólio de um prestador.
 * Cada item representa um trabalho realizado, com foto e descrição.
 *
 * Integração futura:
 * - URL da foto armazenada no AWS S3 (módulo de upload).
 * - Exibida na tela pública do prestador (Tarefa 4 / busca).
 */
@Entity
@Table(name = "portfolio_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_profile_id", nullable = false)
    private ProviderProfile providerProfile;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** URL pública da imagem (S3, CDN ou qualquer storage) */
    @Column
    private String photoUrl;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}

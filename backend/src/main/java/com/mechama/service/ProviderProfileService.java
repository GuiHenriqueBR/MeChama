package com.mechama.service;

import com.mechama.dto.*;
import com.mechama.model.PortfolioItem;
import com.mechama.model.ProviderProfile;
import com.mechama.model.User;
import com.mechama.model.UserType;
import com.mechama.repository.PortfolioItemRepository;
import com.mechama.repository.ProviderProfileRepository;
import com.mechama.repository.ServiceOfferingRepository;
import com.mechama.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço de perfil de prestador e portfólio.
 *
 * Integrações com módulos anteriores:
 * - UserRepository (Tarefa 1): carrega o User vinculado ao perfil
 *
 * Integrações com módulos futuros:
 * - Tarefa 3: ServiceOffering será adicionado ao response do perfil
 * - Tarefa 4: avgRating e specialties alimentam o score de busca
 * - Tarefa 8: updateRating() é chamado pelo módulo de avaliações
 * - Tarefa 9: toggleAvailability() é acionado pelo painel do prestador
 */
@Service
@RequiredArgsConstructor
public class ProviderProfileService {

    private final ProviderProfileRepository profileRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final UserRepository userRepository;

    // ─── Perfil ─────────────────────────────────────────────────────────────────

    /**
     * Retorna o perfil de um prestador pelo ID do usuário.
     * Público: usado na tela de detalhe do prestador (sem autenticação).
     */
    @Transactional(readOnly = true)
    public ProviderProfileResponse getProfileByUserId(Long userId) {
        ProviderProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de prestador não encontrado"));
        return toResponse(profile);
    }

    /**
     * Retorna o perfil do próprio prestador autenticado.
     * Usado na tela de edição do perfil.
     */
    @Transactional(readOnly = true)
    public ProviderProfileResponse getMyProfile(Long authenticatedUserId) {
        return getProfileByUserId(authenticatedUserId);
    }

    /**
     * Cria ou atualiza o perfil do prestador autenticado.
     * Chamado pelo controller via PUT /api/providers/me/profile.
     */
    @Transactional
    public ProviderProfileResponse upsertProfile(Long authenticatedUserId, ProviderProfileRequest request) {
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (user.getType() != UserType.PROVIDER) {
            throw new IllegalArgumentException("Apenas prestadores podem ter perfil profissional");
        }

        ProviderProfile profile = profileRepository.findByUserId(authenticatedUserId)
                .orElse(ProviderProfile.builder().user(user).build());

        applyUpdates(profile, request);
        profileRepository.save(profile);
        return toResponse(profile);
    }

    /**
     * Aplica campos não-nulos do request no perfil existente (merge parcial).
     * Permite atualizar apenas os campos enviados (PATCH semântico).
     */
    private void applyUpdates(ProviderProfile profile, ProviderProfileRequest request) {
        if (request.getBio() != null)              profile.setBio(request.getBio());
        if (request.getAvatarUrl() != null)        profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getCity() != null)             profile.setCity(request.getCity());
        if (request.getNeighborhood() != null)     profile.setNeighborhood(request.getNeighborhood());
        if (request.getExperienceYears() != null)  profile.setExperienceYears(request.getExperienceYears());
        if (request.getSpecialties() != null)      profile.setSpecialties(request.getSpecialties());
        if (request.getServiceRadiusKm() != null)  profile.setServiceRadiusKm(request.getServiceRadiusKm());
        if (request.getAvailable() != null)        profile.setAvailable(request.getAvailable());
    }

    // ─── Portfólio ──────────────────────────────────────────────────────────────

    /**
     * Lista itens de portfólio de um prestador — acesso público.
     */
    @Transactional(readOnly = true)
    public List<PortfolioItemResponse> getPortfolio(Long userId) {
        ProviderProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de prestador não encontrado"));

        return portfolioItemRepository
                .findByProviderProfileIdOrderByCreatedAtDesc(profile.getId())
                .stream()
                .map(this::toPortfolioResponse)
                .toList();
    }

    /**
     * Adiciona item ao portfólio do prestador autenticado.
     */
    @Transactional
    public PortfolioItemResponse addPortfolioItem(Long authenticatedUserId, PortfolioItemRequest request) {
        ProviderProfile profile = profileRepository.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de prestador não encontrado. Complete seu perfil primeiro."));

        PortfolioItem item = PortfolioItem.builder()
                .providerProfile(profile)
                .title(request.getTitle())
                .description(request.getDescription())
                .photoUrl(request.getPhotoUrl())
                .build();

        portfolioItemRepository.save(item);
        return toPortfolioResponse(item);
    }

    /**
     * Remove item do portfólio — apenas o dono pode excluir.
     */
    @Transactional
    public void deletePortfolioItem(Long authenticatedUserId, Long itemId) {
        ProviderProfile profile = profileRepository.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de prestador não encontrado"));

        boolean isOwner = portfolioItemRepository.existsByIdAndProviderProfileId(itemId, profile.getId());
        if (!isOwner) {
            throw new IllegalArgumentException("Item não encontrado ou não pertence ao prestador");
        }

        portfolioItemRepository.deleteById(itemId);
    }

    // ─── Chamado pela Tarefa 8 (Avaliações) ─────────────────────────────────────

    /**
     * Recalcula e persiste a média de avaliação do prestador.
     * Será chamado pelo ReviewService sempre que uma nova avaliação for criada.
     */
    @Transactional
    public void updateRating(Long userId, double newAvgRating, int newTotalReviews) {
        ProviderProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado"));
        profile.setAvgRating(newAvgRating);
        profile.setTotalReviews(newTotalReviews);
        profileRepository.save(profile);
    }

    // ─── Usado internamente pelo UserService (Tarefa 1) ─────────────────────────

    /**
     * Cria um perfil vazio para um prestador recém-cadastrado.
     * Chamado por UserService.register() quando type == PROVIDER.
     */
    @Transactional
    public void createEmptyProfile(User user) {
        if (profileRepository.existsByUserId(user.getId())) return;
        ProviderProfile profile = ProviderProfile.builder().user(user).build();
        profileRepository.save(profile);
    }

    // ─── Mapeamento interno ─────────────────────────────────────────────────────

    private ProviderProfileResponse toResponse(ProviderProfile p) {
        List<PortfolioItemResponse> items = p.getPortfolioItems().stream()
                .map(this::toPortfolioResponse)
                .toList();

        // Tarefa 3: carrega serviços ativos do prestador para compor a resposta do perfil
        List<ServiceOfferingResponse> services = serviceOfferingRepository
                .findByProviderProfileIdAndActiveTrueOrderByCreatedAtDesc(p.getId())
                .stream()
                .map(s -> ServiceOfferingResponse.builder()
                        .id(s.getId())
                        .providerUserId(p.getUser().getId())
                        .providerName(p.getUser().getName())
                        .providerAvatarUrl(p.getAvatarUrl())
                        .providerAvgRating(p.getAvgRating())
                        .providerTotalReviews(p.getTotalReviews())
                        .providerCity(p.getCity())
                        .categoryId(s.getCategory().getId())
                        .categoryName(s.getCategory().getName())
                        .categoryIcon(s.getCategory().getIcon())
                        .categorySlug(s.getCategory().getSlug())
                        .title(s.getTitle())
                        .description(s.getDescription())
                        .basePrice(s.getBasePrice())
                        .durationMinutes(s.getDurationMinutes())
                        .whatIsIncluded(s.getWhatIsIncluded())
                        .whatIsNotIncluded(s.getWhatIsNotIncluded())
                        .active(s.isActive())
                        .createdAt(s.getCreatedAt())
                        .updatedAt(s.getUpdatedAt())
                        .build())
                .toList();

        return ProviderProfileResponse.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .userName(p.getUser().getName())
                .userEmail(p.getUser().getEmail())
                .bio(p.getBio())
                .avatarUrl(p.getAvatarUrl())
                .city(p.getCity())
                .neighborhood(p.getNeighborhood())
                .experienceYears(p.getExperienceYears())
                .specialties(p.getSpecialties())
                .avgRating(p.getAvgRating())
                .totalReviews(p.getTotalReviews())
                .serviceRadiusKm(p.getServiceRadiusKm())
                .available(p.isAvailable())
                .portfolioItems(items)
                .services(services)
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private PortfolioItemResponse toPortfolioResponse(PortfolioItem item) {
        return PortfolioItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .photoUrl(item.getPhotoUrl())
                .createdAt(item.getCreatedAt())
                .build();
    }
}

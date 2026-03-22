package com.mechama.service;

import com.mechama.dto.ServiceCategoryResponse;
import com.mechama.dto.ServiceOfferingRequest;
import com.mechama.dto.ServiceOfferingResponse;
import com.mechama.model.ProviderProfile;
import com.mechama.model.ServiceCategory;
import com.mechama.model.ServiceOffering;
import com.mechama.repository.ProviderProfileRepository;
import com.mechama.repository.ServiceCategoryRepository;
import com.mechama.repository.ServiceOfferingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço de catálogo — gerencia ServiceOffering e ServiceCategory.
 *
 * Regra crítica implementada aqui:
 * - basePrice >= category.minPrice (validação anti-dumping antes de persistir)
 *
 * Integrações com módulos anteriores:
 * - ProviderProfileRepository (T2): localiza o perfil do prestador autenticado
 *
 * Integrações com módulos futuros:
 * - Tarefa 4 (Busca): findActiveByCategory() já está exposto no repositório
 * - Tarefa 6 (Ordens): Order.serviceId referencia ServiceOffering.id
 * - Tarefa 9 (Painel): getMyServices() alimenta a lista de serviços do prestador
 */
@Service
@RequiredArgsConstructor
public class ServiceOfferingService {

    private final ServiceOfferingRepository serviceRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final ProviderProfileRepository profileRepository;

    // ─── Categorias ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ServiceCategoryResponse> listCategories() {
        return categoryRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    // ─── Serviços — acesso público ───────────────────────────────────────────────

    /**
     * Lista serviços ativos de um prestador pelo userId.
     * Usado na tela pública de detalhe do prestador (sem autenticação).
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> getActiveServicesByProvider(Long userId) {
        ProviderProfile profile = findProfileByUserId(userId);
        return serviceRepository
                .findByProviderProfileIdAndActiveTrueOrderByCreatedAtDesc(profile.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Detalhe de um serviço — público, sem autenticação.
     */
    @Transactional(readOnly = true)
    public ServiceOfferingResponse getById(Long serviceId) {
        ServiceOffering service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));
        return toResponse(service);
    }

    // ─── Serviços — acesso do prestador autenticado ──────────────────────────────

    /**
     * Lista todos os serviços do prestador autenticado (incluindo inativos).
     * Usado no painel do prestador (Tarefa 9).
     */
    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> getMyServices(Long authenticatedUserId) {
        ProviderProfile profile = findProfileByUserId(authenticatedUserId);
        return serviceRepository
                .findByProviderProfileIdOrderByCreatedAtDesc(profile.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Cria um novo serviço.
     * Valida que o preço não está abaixo do piso da categoria (anti-dumping).
     */
    @Transactional
    public ServiceOfferingResponse create(Long authenticatedUserId, ServiceOfferingRequest request) {
        ProviderProfile profile = findProfileByUserId(authenticatedUserId);
        ServiceCategory category = findCategory(request.getCategoryId());

        validateMinPrice(request, category);

        ServiceOffering service = ServiceOffering.builder()
                .providerProfile(profile)
                .category(category)
                .title(request.getTitle())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .durationMinutes(request.getDurationMinutes())
                .whatIsIncluded(request.getWhatIsIncluded())
                .whatIsNotIncluded(request.getWhatIsNotIncluded())
                .active(true)
                .build();

        serviceRepository.save(service);
        return toResponse(service);
    }

    /**
     * Atualiza um serviço existente — somente o dono pode editar.
     */
    @Transactional
    public ServiceOfferingResponse update(Long authenticatedUserId, Long serviceId, ServiceOfferingRequest request) {
        ProviderProfile profile = findProfileByUserId(authenticatedUserId);
        ServiceOffering service = findAndVerifyOwnership(serviceId, profile.getId());
        ServiceCategory category = findCategory(request.getCategoryId());

        validateMinPrice(request, category);

        service.setCategory(category);
        service.setTitle(request.getTitle());
        service.setDescription(request.getDescription());
        service.setBasePrice(request.getBasePrice());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setWhatIsIncluded(request.getWhatIsIncluded());
        service.setWhatIsNotIncluded(request.getWhatIsNotIncluded());
        if (request.getActive() != null) {
            service.setActive(request.getActive());
        }

        serviceRepository.save(service);
        return toResponse(service);
    }

    /**
     * Ativa ou desativa um serviço sem excluí-lo (soft toggle).
     * Serviços desativados não aparecem na busca nem no perfil público.
     */
    @Transactional
    public ServiceOfferingResponse toggleActive(Long authenticatedUserId, Long serviceId) {
        ProviderProfile profile = findProfileByUserId(authenticatedUserId);
        ServiceOffering service = findAndVerifyOwnership(serviceId, profile.getId());
        service.setActive(!service.isActive());
        serviceRepository.save(service);
        return toResponse(service);
    }

    /**
     * Exclui permanentemente um serviço.
     * Na prática, preferir desativar (toggleActive) para manter histórico de ordens.
     */
    @Transactional
    public void delete(Long authenticatedUserId, Long serviceId) {
        ProviderProfile profile = findProfileByUserId(authenticatedUserId);
        findAndVerifyOwnership(serviceId, profile.getId()); // só garante propriedade
        serviceRepository.deleteById(serviceId);
    }

    // ─── Helpers privados ────────────────────────────────────────────────────────

    /**
     * Regra crítica anti-dumping: rejeita serviço abaixo do preço mínimo da categoria.
     */
    private void validateMinPrice(ServiceOfferingRequest request, ServiceCategory category) {
        if (request.getBasePrice().compareTo(category.getMinPrice()) < 0) {
            throw new IllegalArgumentException(
                String.format(
                    "O preço mínimo para a categoria '%s' é R$ %.2f. " +
                    "Valor informado: R$ %.2f.",
                    category.getName(),
                    category.getMinPrice(),
                    request.getBasePrice()
                )
            );
        }
    }

    private ProviderProfile findProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Perfil de prestador não encontrado. Complete seu perfil primeiro."));
    }

    private ServiceCategory findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));
    }

    private ServiceOffering findAndVerifyOwnership(Long serviceId, Long profileId) {
        ServiceOffering service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));

        if (!service.getProviderProfile().getId().equals(profileId)) {
            throw new IllegalArgumentException("Serviço não pertence a este prestador");
        }
        return service;
    }

    // ─── Mapeamento ──────────────────────────────────────────────────────────────

    private ServiceOfferingResponse toResponse(ServiceOffering s) {
        ProviderProfile p = s.getProviderProfile();
        ServiceCategory c = s.getCategory();

        return ServiceOfferingResponse.builder()
                .id(s.getId())
                // Prestador
                .providerUserId(p.getUser().getId())
                .providerName(p.getUser().getName())
                .providerAvatarUrl(p.getAvatarUrl())
                .providerAvgRating(p.getAvgRating())
                .providerTotalReviews(p.getTotalReviews())
                .providerCity(p.getCity())
                // Categoria
                .categoryId(c.getId())
                .categoryName(c.getName())
                .categoryIcon(c.getIcon())
                .categorySlug(c.getSlug())
                // Serviço
                .title(s.getTitle())
                .description(s.getDescription())
                .basePrice(s.getBasePrice())
                .durationMinutes(s.getDurationMinutes())
                .whatIsIncluded(s.getWhatIsIncluded())
                .whatIsNotIncluded(s.getWhatIsNotIncluded())
                .active(s.isActive())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private ServiceCategoryResponse toCategoryResponse(ServiceCategory c) {
        return ServiceCategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .icon(c.getIcon())
                .minPrice(c.getMinPrice())
                .build();
    }
}

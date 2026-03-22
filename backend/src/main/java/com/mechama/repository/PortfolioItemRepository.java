package com.mechama.repository;

import com.mechama.model.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {

    /** Retorna todos os itens de portfólio de um prestador, do mais recente ao mais antigo */
    List<PortfolioItem> findByProviderProfileIdOrderByCreatedAtDesc(Long providerProfileId);

    /** Garante que o item pertence ao prestador antes de deletar */
    boolean existsByIdAndProviderProfileId(Long itemId, Long providerProfileId);
}

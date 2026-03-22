package com.mechama.repository;

import com.mechama.model.ProviderProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, Long> {

    /** Busca perfil pelo ID do usuário dono */
    Optional<ProviderProfile> findByUserId(Long userId);

    /** Verifica se já existe perfil para determinado usuário */
    boolean existsByUserId(Long userId);

    /**
     * Busca full-text básica por cidade + disponibilidade.
     * Será expandida na Tarefa 4 (Busca e Descoberta) com filtros
     * de categoria, preço e score de ordenação multi-critério.
     */
    @Query("""
        SELECT p FROM ProviderProfile p
        WHERE p.available = true
          AND (:city IS NULL OR LOWER(p.city) LIKE LOWER(CONCAT('%', :city, '%')))
        ORDER BY p.avgRating DESC
        """)
    Page<ProviderProfile> findAvailableByCity(@Param("city") String city, Pageable pageable);
}

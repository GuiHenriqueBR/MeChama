package com.mechama.repository;

import com.mechama.model.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    List<ServiceCategory> findByActiveTrueOrderByNameAsc();

    Optional<ServiceCategory> findBySlug(String slug);

    boolean existsBySlug(String slug);
}

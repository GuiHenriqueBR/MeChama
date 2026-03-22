package com.mechama.repository;

import com.mechama.model.User;
import com.mechama.model.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Útil para buscar todos os prestadores (módulo de busca - Tarefa 4) */
    List<User> findByTypeAndActiveTrue(UserType type);
}

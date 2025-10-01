package com.example.bankcards.repository;
import com.example.bankcards.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для ролей (ROLE_USER, ROLE_ADMIN).
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Находит роль по её имени (например "USER" или "ADMIN").
     */
    Optional<Role> findByName(String name);
}

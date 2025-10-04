package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Page<User> findByUsernameEqualsIgnoreCase(String username, Pageable pageable);
    Page<User> findByUsernameStartingWithIgnoreCase(String username, Pageable pageable);
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    Page<User> findByEnabled(boolean enabled, Pageable pageable);
    Page<User> findByUsernameContainingIgnoreCaseAndEnabled(String username, boolean enabled, Pageable pageable);
}

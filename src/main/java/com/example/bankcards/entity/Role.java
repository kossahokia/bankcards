package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a user role within the application.
 * <p>
 * Roles define access levels and permissions in the system
 * (e.g. {@code USER}, {@code ADMIN}).
 * Each {@link com.example.bankcards.entity.User} can have one or more roles.
 * </p>
 *
 * <p>Table: {@code roles}</p>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    /**
     * Primary key — unique identifier of the role.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the role (e.g. {@code USER}, {@code ADMIN}).
     * Must be unique.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;
}

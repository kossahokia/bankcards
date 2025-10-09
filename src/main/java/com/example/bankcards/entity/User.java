package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing an application user.
 * <p>
 * Each user can own multiple {@link Card} entities and hold one or more {@link Role}s.
 * User accounts are secured using hashed passwords and JWT-based authentication.
 * </p>
 *
 * <p>
 * The {@code createdAt} field is automatically initialized when the entity is first persisted.
 * Users can be enabled or disabled by administrators for access control.
 * </p>
 *
 * <p>Table: {@code users}</p>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Primary key — unique identifier of the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique username used for authentication.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    /**
     * Securely hashed password of the user.
     * Stored using {@code BCryptPasswordEncoder}.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Full name of the user for display and identification.
     */
    @Column(name = "full_name", length = 200)
    private String fullName;

    /**
     * Timestamp of account creation.
     * Automatically set before persisting the entity.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Indicates whether the user account is active.
     * Disabled users cannot authenticate or access the system.
     */
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /**
     * Roles assigned to the user (e.g. {@code USER}, {@code ADMIN}).
     * Loaded eagerly for authorization checks.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * Cards owned by the user.
     * <p>
     * Cascade and orphan removal are enabled to ensure that when a user is deleted,
     * all associated cards are removed as well.
     * </p>
     */
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Card> cards = new HashSet<>();

    /**
     * Automatically sets {@link #createdAt} before persisting the entity.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Associates a new {@link Card} with this user.
     *
     * @param card card to add
     */
    public void addCard(Card card) {
        cards.add(card);
        card.setOwner(this);
    }

    /**
     * Removes a {@link Card} from this user.
     *
     * @param card card to remove
     */
    public void removeCard(Card card) {
        cards.remove(card);
        card.setOwner(null);
    }
}

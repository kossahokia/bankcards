package com.example.bankcards.security;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom implementation of Spring Security's {@link UserDetails} interface,
 * used to adapt the application's {@link User} entity to the security framework.
 * <p>
 * This class wraps a {@link User} object and exposes its data (username, password,
 * roles, enabled status) in a format compatible with Spring Security.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Translate {@link Role} entities into {@link GrantedAuthority} objects with prefix {@code ROLE_}.</li>
 *   <li>Provide access to authentication-related information (username, password, enabled flag).</li>
 *   <li>Serve as the bridge between domain model and Spring Security authentication mechanisms.</li>
 * </ul>
 *
 * <h3>Example usage:</h3>
 * <pre>{@code
 * Authentication authentication =
 *     new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
 * }</pre>
 *
 * @see User
 * @see Role
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see com.example.bankcards.security.UserDetailsServiceImpl
 * @see com.example.bankcards.security.JwtAuthenticationFilter
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private final User user;

    /**
     * Converts the user's roles into Spring Security authorities.
     *
     * @return a collection of {@link GrantedAuthority} representing the user's roles
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = user.getRoles();
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet());
    }

    /**
     * @return the user's hashed password
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * @return the user's unique username
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * Always returns {@code true} — account expiration is not managed.
     * May be extended in future versions.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Always returns {@code true} — account locking is not managed.
     * May be extended in future versions.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Always returns {@code true} — credential expiration is not managed.
     * May be extended in future versions.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * @return {@code true} if the user account is enabled; otherwise {@code false}
     */
    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    /**
     * Provides access to the underlying {@link User} entity.
     *
     * @return the wrapped domain {@link User} instance
     */
    public User getUser() {
        return this.user;
    }
}

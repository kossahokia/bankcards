package com.example.bankcards.service;

import com.example.bankcards.dto.enums.UsernameMatchType;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.customexceptions.BadRequestException;
import com.example.bankcards.exception.customexceptions.NotFoundException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(String username, String password, String fullName, String roleName, boolean enabled) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BadRequestException("User with this username already exists");
        }

        Role role = roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .roles(roles)
                .enabled(enabled)
                .build();

        return userRepository.save(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public Page<User> getAllUsers(String username, Boolean enabled, UsernameMatchType matchType, Pageable pageable) {
        if (username != null && enabled != null) {
            return switch (matchType != null ? matchType : UsernameMatchType.CONTAINS) {
                case EQUALS -> userRepository.findByUsernameEqualsIgnoreCase(username, pageable)
                        .map(u -> { u.setEnabled(u.isEnabled() && enabled); return u; });
                case STARTS -> userRepository.findByUsernameStartingWithIgnoreCase(username, pageable)
                        .map(u -> { u.setEnabled(u.isEnabled() && enabled); return u; });
                case CONTAINS -> userRepository.findByUsernameContainingIgnoreCase(username, pageable)
                        .map(u -> { u.setEnabled(u.isEnabled() && enabled); return u; });
            };
        } else if (username != null) {
            return switch (matchType != null ? matchType : UsernameMatchType.CONTAINS) {
                case EQUALS -> userRepository.findByUsernameEqualsIgnoreCase(username, pageable);
                case STARTS -> userRepository.findByUsernameStartingWithIgnoreCase(username, pageable);
                case CONTAINS -> userRepository.findByUsernameContainingIgnoreCase(username, pageable);
            };
        } else if (enabled != null) {
            return userRepository.findByEnabled(enabled, pageable);
        } else {
            return userRepository.findAll(pageable);
        }
    }


    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.getRoles().clear();
        userRepository.delete(user);
    }

    public User assignRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Role role = roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));

        user.getRoles().add(role);
        return userRepository.save(user);
    }

    public User removeRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Role role = roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        user.getRoles().remove(role);
        return userRepository.save(user);
    }

    public User updateUserEnabledStatus(Long userId, boolean status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setEnabled(status);
        return userRepository.save(user);
    }
}

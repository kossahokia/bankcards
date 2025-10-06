package com.example.bankcards.service;

import com.example.bankcards.dto.enums.UsernameMatchType;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.customexceptions.BadRequestException;
import com.example.bankcards.exception.customexceptions.NotFoundException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private User user;
    private Role role;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("alice")
                .password("encodedPass")
                .fullName("Alice A.")
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        role = new Role();
        role.setId(1L);
        role.setName("USER");

        pageable = PageRequest.of(0, 10);

        lenient().when(passwordEncoder.encode(anyString()))
                .thenAnswer(i -> "ENC(" + i.getArgument(0) + ")");
    }

    // --- createUser ---

    @Test
    @DisplayName("✅ createUser success")
    void createUser_Success() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User saved = userService.createUser("alice", "pwd", "Alice", "USER", true);

        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getPassword()).isEqualTo("ENC(pwd)");
        assertThat(saved.getRoles()).contains(role);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("❌ createUser throws if username exists")
    void createUser_UsernameExists_Throws() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                userService.createUser("alice", "pwd", "Alice", "USER", true)
        ).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("❌ createUser throws if role not found")
    void createUser_RoleNotFound_Throws() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.createUser("alice", "pwd", "Alice", "USER", true)
        ).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Role not found");
    }

    // --- getUserById & findByUsername ---

    @Test
    @DisplayName("✅ getUserById success")
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThat(userService.getUserById(1L)).isEqualTo(user);
    }

    @Test
    @DisplayName("❌ getUserById throws if not found")
    void getUserById_NotFound_Throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("✅ findByUsername success")
    void findByUsername_Success() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        assertThat(userService.findByUsername("alice")).isEqualTo(user);
    }

    @Test
    @DisplayName("❌ findByUsername throws if not found")
    void findByUsername_NotFound_Throws() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findByUsername("alice"))
                .isInstanceOf(NotFoundException.class);
    }

    // --- getAllUsers (all branches) ---

    private Page<User> makePage(User... users) {
        return new PageImpl<>(Arrays.asList(users), pageable, users.length);
    }

    @Test
    @DisplayName("✅ getAllUsers username+enabled with EQUALS")
    void getAllUsers_UsernameEnabled_Equals() {
        when(userRepository.findByUsernameEqualsIgnoreCase(eq("alice"), any()))
                .thenReturn(makePage(user));

        Page<User> result = userService.getAllUsers("alice", true, UsernameMatchType.EQUALS, pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("✅ getAllUsers username+enabled with STARTS")
    void getAllUsers_UsernameEnabled_Starts() {
        when(userRepository.findByUsernameStartingWithIgnoreCase(eq("a"), any()))
                .thenReturn(makePage(user));

        Page<User> result = userService.getAllUsers("a", true, UsernameMatchType.STARTS, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("✅ getAllUsers username+enabled with CONTAINS (default)")
    void getAllUsers_UsernameEnabled_Contains_DefaultMatchType() {
        when(userRepository.findByUsernameContainingIgnoreCase(eq("a"), any()))
                .thenReturn(makePage(user));

        Page<User> result = userService.getAllUsers("a", true, null, pageable);
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("✅ getAllUsers username only with EQUALS")
    void getAllUsers_UsernameOnly_Equals() {
        when(userRepository.findByUsernameEqualsIgnoreCase(eq("alice"), any()))
                .thenReturn(makePage(user));

        Page<User> result = userService.getAllUsers("alice", null, UsernameMatchType.EQUALS, pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("✅ getAllUsers username only with STARTS")
    void getAllUsers_UsernameOnly_Starts() {
        when(userRepository.findByUsernameStartingWithIgnoreCase(eq("a"), any()))
                .thenReturn(makePage(user));

        Page<User> result = userService.getAllUsers("a", null, UsernameMatchType.STARTS, pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("✅ getAllUsers username only with CONTAINS (default)")
    void getAllUsers_UsernameOnly_Contains_Default() {
        when(userRepository.findByUsernameContainingIgnoreCase(eq("a"), any()))
                .thenReturn(makePage(user));

        Page<User> result = userService.getAllUsers("a", null, null, pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("✅ getAllUsers enabled only")
    void getAllUsers_EnabledOnly() {
        when(userRepository.findByEnabled(true, pageable)).thenReturn(makePage(user));
        Page<User> result = userService.getAllUsers(null, true, null, pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("✅ getAllUsers no filters")
    void getAllUsers_NoFilters() {
        when(userRepository.findAll(pageable)).thenReturn(makePage(user));
        Page<User> result = userService.getAllUsers(null, null, null, pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    // --- deleteUser ---

    @Test
    @DisplayName("✅ deleteUser success")
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        user.getRoles().add(role);

        userService.deleteUser(1L);

        assertThat(user.getRoles()).isEmpty();
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("❌ deleteUser throws if user not found")
    void deleteUser_NotFound_Throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(NotFoundException.class);
    }

    // --- assignRole ---

    @Test
    @DisplayName("✅ assignRole success")
    void assignRole_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = userService.assignRole(1L, "USER");

        assertThat(updated.getRoles()).contains(role);
    }

    @Test
    @DisplayName("❌ assignRole user not found")
    void assignRole_UserNotFound_Throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.assignRole(1L, "USER"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("❌ assignRole role not found")
    void assignRole_RoleNotFound_Throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.assignRole(1L, "USER"))
                .isInstanceOf(NotFoundException.class);
    }

    // --- removeRole ---

    @Test
    @DisplayName("✅ removeRole success")
    void removeRole_Success() {
        user.getRoles().add(role);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = userService.removeRole(1L, "USER");

        assertThat(updated.getRoles()).doesNotContain(role);
    }

    @Test
    @DisplayName("❌ removeRole user not found")
    void removeRole_UserNotFound_Throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.removeRole(1L, "USER"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("❌ removeRole role not found")
    void removeRole_RoleNotFound_Throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.removeRole(1L, "USER"))
                .isInstanceOf(NotFoundException.class);
    }

    // --- updateUserEnabledStatus ---

    @Test
    @DisplayName("✅ updateUserEnabledStatus success")
    void updateUserEnabledStatus_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = userService.updateUserEnabledStatus(1L, false);

        assertThat(updated.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("❌ updateUserEnabledStatus user not found")
    void updateUserEnabledStatus_NotFound_Throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateUserEnabledStatus(1L, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("✅ getAllUsers username+enabled=false (EQUALS) disables users correctly")
    void getAllUsers_UsernameEnabledFalse_DisablesUser() {
        user.setEnabled(true);
        when(userRepository.findByUsernameEqualsIgnoreCase(eq("alice"), any()))
                .thenReturn(makePage(user));

        Page<User> result = userService.getAllUsers("alice", false, UsernameMatchType.EQUALS, pageable);

        // теперь .map(u -> u.setEnabled(u.isEnabled() && enabled)) отработал со значением false
        assertThat(result.getContent().get(0).isEnabled()).isFalse();
    }

    @Test
    @DisplayName("✅ getAllUsers username+enabled=false with STARTS handles disabled user")
    void getAllUsers_UsernameEnabledFalse_Starts_DisablesUser() {
        user.setEnabled(false);
        when(userRepository.findByUsernameStartingWithIgnoreCase(eq("a"), any()))
                .thenReturn(makePage(user));

        Page<User> result = userService.getAllUsers("a", false, UsernameMatchType.STARTS, pageable);

        assertThat(result.getContent().get(0).isEnabled()).isFalse();
    }
    @Test
    @DisplayName("✅ getAllUsers username+enabled with CONTAINS and null matchType (branch coverage fix)")
    void getAllUsers_UsernameEnabled_Contains_NullMatchType() {
        when(userRepository.findByUsernameContainingIgnoreCase(eq("alice"), any()))
                .thenReturn(makePage(user));

        // matchType = null, enabled != null (true)
        Page<User> result = userService.getAllUsers("alice", true, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isEnabled()).isTrue();
    }
    @Test
    @DisplayName("✅ getAllUsers enabled only (false)")
    void getAllUsers_EnabledOnly_False() {
        when(userRepository.findByEnabled(false, pageable)).thenReturn(makePage(user));
        Page<User> result = userService.getAllUsers(null, false, null, pageable);
        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).findByEnabled(false, pageable);
    }
    @Test
    @DisplayName("✅ getAllUsers username+enabled=false with CONTAINS")
    void getAllUsers_UsernameEnabledFalse_Contains() {
        user.setEnabled(true);
        when(userRepository.findByUsernameContainingIgnoreCase(eq("alice"), any()))
                .thenReturn(makePage(user));

        Page<User> result = userService.getAllUsers("alice", false, UsernameMatchType.CONTAINS, pageable);

        assertThat(result.getContent().get(0).isEnabled()).isFalse();
    }
    @Test
    @DisplayName("❌ assignRole role not found includes role name in message")
    void assignRole_RoleNotFound_MessageCheck() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignRole(1L, "USER"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Role not found: USER");
    }
    @Test
    @DisplayName("❌ removeRole role not found includes correct message")
    void removeRole_RoleNotFound_MessageCheck() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.removeRole(1L, "USER"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Role not found");

    }
    @Test
    @DisplayName("✅ getAllUsers username+enabled=true with user initially disabled")
    void getAllUsers_UsernameEnabledTrue_UserInitiallyDisabled() {
        user.setEnabled(false); // пользователь изначально выключен
        when(userRepository.findByUsernameEqualsIgnoreCase(eq("alice"), any()))
                .thenReturn(makePage(user));

        Page<User> result = userService.getAllUsers("alice", true, UsernameMatchType.EQUALS, pageable);

        // enabled = true, но u.isEnabled() = false → останется false
        assertThat(result.getContent().get(0).isEnabled()).isFalse();
    }
    @Test
    @DisplayName("✅ getAllUsers when both username and enabled are null → findAll() called")
    void getAllUsers_BothNull_CallsFindAll() {
        when(userRepository.findAll(pageable)).thenReturn(makePage(user));

        Page<User> result = userService.getAllUsers(null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).findAll(pageable);
    }
    @Test
    @DisplayName("✅ getAllUsers username only + null matchType defaults to CONTAINS")
    void getAllUsers_UsernameOnly_NullMatchType_DefaultContains() {
        when(userRepository.findByUsernameContainingIgnoreCase(eq("alice"), any()))
                .thenReturn(makePage(user));

        Page<User> result = userService.getAllUsers("alice", null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).findByUsernameContainingIgnoreCase("alice", pageable);
    }
    @Test
    @DisplayName("✅ getAllUsers username+enabled=true with user initially disabled (STARTS)")
    void getAllUsers_UsernameEnabledTrue_UserInitiallyDisabled_Starts() {
        user.setEnabled(false);
        when(userRepository.findByUsernameStartingWithIgnoreCase(eq("a"), any()))
                .thenReturn(makePage(user));

        Page<User> result = userService.getAllUsers("a", true, UsernameMatchType.STARTS, pageable);

        assertThat(result.getContent().get(0).isEnabled()).isFalse();
        verify(userRepository).findByUsernameStartingWithIgnoreCase("a", pageable);
    }
    @Test
    @DisplayName("✅ getAllUsers username+enabled=true with user initially disabled (CONTAINS)")
    void getAllUsers_UsernameEnabledTrue_UserInitiallyDisabled_Contains() {
        user.setEnabled(false);
        when(userRepository.findByUsernameContainingIgnoreCase(eq("a"), any()))
                .thenReturn(makePage(user));

        Page<User> result = userService.getAllUsers("a", true, UsernameMatchType.CONTAINS, pageable);

        assertThat(result.getContent().get(0).isEnabled()).isFalse();
        verify(userRepository).findByUsernameContainingIgnoreCase("a", pageable);
    }
}

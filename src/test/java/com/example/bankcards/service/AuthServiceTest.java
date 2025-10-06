package com.example.bankcards.service;

import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.dto.RegisterResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.customexceptions.BadRequestException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthService authService;

    private RegisterRequest validRequest;
    private Role roleUser;
    private User savedUser;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest();
        validRequest.setUsername("alice");
        validRequest.setPassword("pwd123");
        validRequest.setFullName("Alice Wonderland");

        roleUser = new Role();
        roleUser.setId(1L);
        roleUser.setName("USER");

        savedUser = User.builder()
                .id(10L)
                .username("alice")
                .fullName("Alice Wonderland")
                .roles(Set.of(roleUser))
                .enabled(true)
                .build();
    }

    // ---------------- AUTHENTICATE -----------------

    @Test
    @DisplayName("✅ authenticate success returns token")
    void authenticate_Success() {
        Authentication mockAuth = mock(Authentication.class);
        UserDetailsImpl mockPrincipal = mock(UserDetailsImpl.class);

        when(mockAuth.getPrincipal()).thenReturn(mockPrincipal);
        when(mockPrincipal.getUsername()).thenReturn("alice");
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(jwtTokenProvider.generateToken("alice")).thenReturn("TOKEN_123");

        String token = authService.authenticate("alice", "pwd123");

        assertThat(token).isEqualTo("TOKEN_123");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken("alice");
    }

    @Test
    @DisplayName("❌ authenticate throws BadCredentialsException when authentication fails")
    void authenticate_Failure_ThrowsBadCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> authService.authenticate("bob", "wrong"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid username or password");
    }

    // ---------------- REGISTER -----------------

    @Test
    @DisplayName("✅ register success saves user and returns response")
    void register_Success() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("pwd123")).thenReturn("ENC(pwd123)");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });

        RegisterResponse response = authService.register(validRequest);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getFullName()).isEqualTo("Alice Wonderland");
        assertThat(response.getRole()).isEqualTo("USER");

        verify(passwordEncoder).encode("pwd123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("❌ register throws if username blank")
    void register_BlankUsername_Throws() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(" ");
        req.setPassword("pwd");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("❌ register throws if password blank")
    void register_BlankPassword_Throws() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword(" ");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("❌ register throws if username already taken")
    void register_UsernameTaken_Throws() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(savedUser));

        assertThatThrownBy(() -> authService.register(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    @DisplayName("❌ register throws if default role USER not found")
    void register_RoleNotFound_Throws() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Default role USER not found");
    }
    @Test
    @DisplayName("⚠️ authenticate throws ClassCastException if principal not UserDetailsImpl")
    void authenticate_PrincipalWrongType_ThrowsClassCast() {
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getPrincipal()).thenReturn("NotAUserDetails");
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);

        assertThatThrownBy(() -> authService.authenticate("john", "pass"))
                .isInstanceOf(ClassCastException.class);
    }
    @Test
    @DisplayName("❌ register throws if username is null")
    void register_NullUsername_Throws() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(null);
        req.setPassword("pwd");
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("❌ register throws if password is null")
    void register_NullPassword_Throws() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword(null);
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must not be blank");
    }

}

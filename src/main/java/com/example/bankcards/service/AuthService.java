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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public String authenticate(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return jwtTokenProvider.generateToken(userDetails.getUsername());
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password", e);
        }
    }

    public RegisterResponse register(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Username and password must not be blank");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username is already taken");
        }

        Role userRole = roleRepository.findByNameIgnoreCase("USER")
                .orElseThrow(() -> new BadRequestException("Default role USER not found"));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .roles(Collections.singleton(userRole))
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getFullName(),
                userRole.getName()
        );
    }
}

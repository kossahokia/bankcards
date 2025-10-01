package com.example.bankcards.service;

import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Аутентификация пользователя и генерация JWT токена
     */
    public String authenticate(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            return jwtTokenProvider.generateToken(userDetails.getUsername());
        } catch (AuthenticationException e) {
            throw new RuntimeException("Неверное имя пользователя или пароль", e);
        }
    }
}

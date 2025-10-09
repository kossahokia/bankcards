package com.example.bankcards.config;

import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Central Spring Security configuration for the BankCards REST API.
 * <p>
 * This class defines authentication, authorization, CORS, CSRF, and
 * session management policies. It integrates JWT authentication via
 * {@link JwtAuthenticationFilter}, uses a {@link DaoAuthenticationProvider}
 * for user credential validation, and enforces stateless security suitable
 * for RESTful applications.
 * </p>
 *
 * <h3>Main Responsibilities:</h3>
 * <ul>
 *     <li>Configure password encoding with {@link BCryptPasswordEncoder}.</li>
 *     <li>Provide an {@link AuthenticationManager} for authentication handling.</li>
 *     <li>Register {@link JwtAuthenticationFilter} before username/password filter.</li>
 *     <li>Permit access to authentication and Swagger documentation endpoints.</li>
 *     <li>Require authentication for all other API requests.</li>
 *     <li>Set security to stateless mode — no HTTP sessions are used.</li>
 * </ul>
 *
 * @see JwtAuthenticationFilter
 * @see UserDetailsServiceImpl
 * @see org.springframework.security.config.annotation.web.builders.HttpSecurity
 * @see org.springframework.security.web.SecurityFilterChain
 * @see org.springframework.security.authentication.AuthenticationManager
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Defines the password encoder used for hashing user credentials.
     *
     * @return a {@link BCryptPasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the authentication provider to use {@link UserDetailsServiceImpl}
     * and the defined {@link PasswordEncoder}.
     *
     * @return a configured {@link DaoAuthenticationProvider}
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Provides the central {@link AuthenticationManager} used to handle login attempts.
     *
     * @param authConfig the Spring Security authentication configuration
     * @return the configured {@link AuthenticationManager}
     * @throws Exception if the authentication manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Defines the main HTTP security filter chain.
     * <p>
     * Configures CSRF and CORS, enforces stateless JWT-based authentication,
     * and defines endpoint-level authorization rules.
     * </p>
     *
     * @param http the {@link HttpSecurity} builder
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if the filter chain cannot be built
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // global CORS settings provided in WebConfig
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/h2-console/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((req, res, ex) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}

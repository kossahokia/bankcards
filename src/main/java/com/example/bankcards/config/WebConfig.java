package com.example.bankcards.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Global Cross-Origin Resource Sharing (CORS) configuration
 * for the BankCards REST API.
 * <p>
 * This configuration allows frontend applications (React, Vue, Angular, etc.)
 * running on local development servers to communicate with the backend API
 * without being blocked by browser CORS restrictions.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Allow requests from specific local origins (e.g., {@code localhost:3000}, {@code localhost:4200}).</li>
 *   <li>Expose standard HTTP methods and headers.</li>
 *   <li>Support credentials (cookies, authorization headers).</li>
 *   <li>Cache preflight (OPTIONS) requests for improved performance.</li>
 * </ul>
 *
 * <p>
 * This configuration integrates automatically with Spring Security
 * (see {@link org.springframework.security.config.annotation.web.builders.HttpSecurity})
 * when {@code http.cors()} is enabled in {@link com.example.bankcards.config.SecurityConfig}.
 * </p>
 *
 * @see org.springframework.web.cors.CorsConfiguration
 * @see org.springframework.security.config.annotation.web.builders.HttpSecurity
 * @see com.example.bankcards.config.SecurityConfig
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Configuration
public class WebConfig {

    /**
     * Defines a {@link CorsConfigurationSource} bean that provides
     * global CORS settings for the entire application.
     *
     * @return a configured {@link CorsConfigurationSource}
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                // React (CRA, Vite)
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:5173",
                // Angular
                "http://localhost:4200",
                "http://127.0.0.1:4200"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // cache preflight responses for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

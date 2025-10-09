package com.example.bankcards.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Application-wide locale configuration for the BankCards REST API.
 * <p>
 * Defines the default language and regional settings used for
 * validation messages, exceptions, and framework-generated responses.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Sets the default {@link Locale} to English ({@code Locale.ENGLISH}).</li>
 *   <li>Ensures all default messages (e.g., validation errors) are displayed in English,
 *       unless explicitly localized by message bundles.</li>
 *   <li>Provides a {@link LocaleResolver} bean that can later support locale switching
 *       (e.g., via HTTP headers or session attributes).</li>
 * </ul>
 *
 * <p>
 * This configuration uses {@link SessionLocaleResolver}, which stores the locale
 * setting in the user’s HTTP session.
 * </p>
 *
 * @see org.springframework.web.servlet.LocaleResolver
 * @see org.springframework.web.servlet.i18n.SessionLocaleResolver
 * @see java.util.Locale
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Configuration
public class LocaleConfig {

    /**
     * Defines the application's default {@link LocaleResolver}.
     * <p>
     * Sets English ({@code en}) as the default locale for all system messages.
     * </p>
     *
     * @return a configured {@link LocaleResolver} instance
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }
}

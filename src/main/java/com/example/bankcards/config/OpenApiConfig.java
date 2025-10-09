package com.example.bankcards.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central OpenAPI (Swagger) configuration for the BankCards REST API.
 * <p>
 * Defines API metadata, JWT authentication scheme, and standard response components
 * to ensure consistent API documentation across all controllers.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Defines API title, version, and license information.</li>
 *   <li>Configures Bearer token (JWT) security for all endpoints.</li>
 *   <li>Registers reusable global HTTP responses (401 Unauthorized, 403 Forbidden).</li>
 *   <li>Integrates with {@link com.example.bankcards.config.SwaggerResponsesConfig}
 *       for consistent security response documentation.</li>
 * </ul>
 *
 * <h3>Security Overview:</h3>
 * <p>
 * All protected endpoints require an HTTP header:
 * <pre>{@code
 * Authorization: Bearer <JWT_TOKEN>
 * }</pre>
 * </p>
 *
 * @see io.swagger.v3.oas.models.OpenAPI
 * @see io.swagger.v3.oas.models.security.SecurityScheme
 * @see com.example.bankcards.config.SwaggerResponsesConfig
 * @see com.example.bankcards.config.SecurityConfig
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Defines the main {@link OpenAPI} bean used by SpringDoc to generate
     * Swagger UI and OpenAPI 3.0 documentation.
     *
     * @return a configured {@link OpenAPI} instance containing metadata,
     *         security schemes, and standard response definitions
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Cards API")
                        .description("REST API for managing bank cards, users, and authentication")
                        .version("1.0.0")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                        .addResponses("Unauthorized", new ApiResponse().description("401 - Unauthorized"))
                        .addResponses("Forbidden", new ApiResponse().description("403 - Forbidden"))
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}

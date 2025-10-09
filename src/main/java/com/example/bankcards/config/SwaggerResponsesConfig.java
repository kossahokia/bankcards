package com.example.bankcards.config;

import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Global Swagger/OpenAPI customization for standardized HTTP responses.
 * <p>
 * This configuration automatically adds default <b>401 Unauthorized</b> and
 * <b>403 Forbidden</b> responses to all secured API endpoints, ensuring
 * consistent documentation across the project.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Automatically applies to all API paths except authentication endpoints ({@code /api/auth/**}).</li>
 *   <li>Ensures every operation in the generated OpenAPI spec contains
 *       standardized 401 and 403 response definitions, if not explicitly declared.</li>
 *   <li>Improves readability and reduces boilerplate in controller-level annotations.</li>
 * </ul>
 *
 * <p>
 * This configuration integrates with SpringDoc’s {@link OpenApiCustomizer}
 * mechanism and runs automatically during the OpenAPI generation process.
 * </p>
 *
 * @see io.swagger.v3.oas.models.OpenAPI
 * @see org.springdoc.core.customizers.OpenApiCustomizer
 * @see com.example.bankcards.config.SecurityConfig
 * @see com.example.bankcards.controller
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Configuration
public class SwaggerResponsesConfig {

    /**
     * Defines a global {@link OpenApiCustomizer} bean that injects
     * standard security-related response codes into all operations.
     *
     * @return a configured {@link OpenApiCustomizer} instance
     */
    @Bean
    public OpenApiCustomizer globalResponsesOpenApiCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) -> {
            // Skip authentication endpoints
            if (path.startsWith("/api/auth")) {
                return;
            }

            pathItem.readOperations().forEach(operation -> {
                ApiResponses responses = operation.getResponses();

                if (!responses.containsKey("401")) {
                    responses.addApiResponse("401", new ApiResponse().description("Unauthorized"));
                }
                if (!responses.containsKey("403")) {
                    responses.addApiResponse("403", new ApiResponse().description("Forbidden"));
                }
            });
        });
    }
}

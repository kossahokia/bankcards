package com.example.bankcards.config;

import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerResponsesConfig {

    @Bean
    public OpenApiCustomizer globalResponsesOpenApiCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) -> {
            // Пропускаем login и register
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

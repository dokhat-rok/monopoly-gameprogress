package com.vpr.monopoly.gameprogress.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурирование объектов для декларирования API с помощью OAS3
 */
@Configuration
public class OpenApiConfig {

    public static final String PROGRESS = "Game Progress";

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Tracking service").version("1.0.0"));
    }
}

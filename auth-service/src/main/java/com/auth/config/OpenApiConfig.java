package com.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    // Cấu hình OpenAPI để Swagger hiển thị API qua gateway.
    @Bean
    public OpenAPI authOpenAPI(@Value("${app.gateway.base-url:http://localhost:9000}") String gatewayBaseUrl) {
        Server gatewayServer = new Server()
                .url(gatewayBaseUrl)
                .description("API Gateway");

        return new OpenAPI()
                .info(new Info().title("Auth Service API").version("v1"))
                .servers(List.of(gatewayServer))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}

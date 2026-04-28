package com.attendance.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    // Cấu hình OpenAPI để Swagger hiển thị API qua gateway.
    @Bean
    public OpenAPI attendanceOpenAPI() {
        Server localServer = new Server()
                .url("http://localhost:9000")
                .description("Local Server");

        return new OpenAPI()
                .info(new Info().title("Request Service API").version("v1").description("Leave request API for Attendance Management System"))
                .servers(List.of(localServer))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}





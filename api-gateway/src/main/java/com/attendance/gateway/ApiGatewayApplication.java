package com.attendance.gateway;

import com.attendance.common.env.DevelopmentEnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        DevelopmentEnvLoader.load();
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}

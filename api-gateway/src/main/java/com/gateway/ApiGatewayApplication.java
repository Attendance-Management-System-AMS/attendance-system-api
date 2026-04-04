package com.gateway;

import com.common.env.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    // Khởi động ứng dụng API Gateway.
    public static void main(String[] args) {
        DotenvLoader.load();
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}

package com.auth;

import com.common.env.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.auth", "com.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class AuthServiceApplication {

    // Khởi động auth-service và nạp .env trước khi chạy Spring Boot.
    public static void main(String[] args) {
        DotenvLoader.load();
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

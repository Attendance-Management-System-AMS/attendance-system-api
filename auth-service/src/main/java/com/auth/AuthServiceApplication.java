package com.auth;

import com.common.env.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.auth", "com.common"})
@EnableDiscoveryClient
@EnableFeignClients
public class AuthServiceApplication {
    public static void main(String[] args) {
        DotenvLoader.load();
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

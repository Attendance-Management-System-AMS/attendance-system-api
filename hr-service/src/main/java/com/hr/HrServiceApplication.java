package com.hr;

import com.common.env.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.hr", "com.common"})
@EnableDiscoveryClient
@EnableFeignClients
public class HrServiceApplication {
    public static void main(String[] args) {
        DotenvLoader.load();
        SpringApplication.run(HrServiceApplication.class, args);
    }
}
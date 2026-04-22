package com.attendance;

import com.attendance.common.env.DevelopmentEnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.attendance.client")
public class HrServiceApplication {

    public static void main(String[] args) {
        DevelopmentEnvLoader.load();
        SpringApplication.run(HrServiceApplication.class, args);
    }
}

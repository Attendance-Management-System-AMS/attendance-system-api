package com.attendance;

import com.common.env.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.attendance", "com.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class AttendanceServiceApplication {
    public static void main(String[] args) {
        DotenvLoader.load();
        SpringApplication.run(AttendanceServiceApplication.class, args);
    }
}

package com.attendance;

import com.attendance.common.env.DevelopmentEnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.attendance")
@EnableFeignClients(basePackages = "com.attendance.client")
@EnableScheduling
@EntityScan(basePackages = "com.attendance.entity")
@EnableJpaRepositories(basePackages = "com.attendance.repository")
public class AttendanceServiceApplication {

    public static void main(String[] args) {
        DevelopmentEnvLoader.load();
        SpringApplication.run(AttendanceServiceApplication.class, args);
    }
}

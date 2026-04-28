package com.attendance.discovery;

import com.attendance.common.env.DevelopmentEnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        DevelopmentEnvLoader.load();
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}

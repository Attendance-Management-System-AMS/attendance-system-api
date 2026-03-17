package com.gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
public class CorsDeduplicationFilter {

    @Bean
    public GlobalFilter deduplicateCorsFilter() {
        return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            List<String> origins = response.getHeaders().get("Access-Control-Allow-Origin");
            if (origins != null && origins.size() > 1) {
                // Giữ lại origin đầu tiên và xóa các bản trùng
                String firstOrigin = origins.get(0);
                response.getHeaders().set("Access-Control-Allow-Origin", firstOrigin);
            }

            List<String> credentials = response.getHeaders().get("Access-Control-Allow-Credentials");
            if (credentials != null && credentials.size() > 1) {
                String firstCred = credentials.get(0);
                response.getHeaders().set("Access-Control-Allow-Credentials", firstCred);
            }
        }));
    }
}

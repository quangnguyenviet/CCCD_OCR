package com.example.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("model-service", r -> r
                        .path("/api/models/**")
                        .uri("lb://model-service"))

                .route("extract-service", r -> r
                        .path("/api/extract/**", "/api/v1/extract/**")
                        .uri("lb://extract-service"))

                .route("ai-service", r -> r
                        .path("/api/ai/**")
                        .uri("lb://AI-service"))
                .build();
    }
}
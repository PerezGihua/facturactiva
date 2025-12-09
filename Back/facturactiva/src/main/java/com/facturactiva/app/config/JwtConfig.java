package com.facturactiva.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {
    private String secret = "5f8a7c6b9d2e4f1a3b7c9d8e6f4a2b1c5d7e9f3a6b8c1d4e7f2a5b9c3d6e8f1a4b";
    private long expiration = 86400000;
    private String tokenPrefix = "Bearer ";
    private String headerString = "Authorization";
}